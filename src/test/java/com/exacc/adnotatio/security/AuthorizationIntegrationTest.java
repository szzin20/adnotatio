package com.exacc.adnotatio.security;

import com.exacc.adnotatio.customer.entity.Customer;
import com.exacc.adnotatio.customer.repository.CustomerRepository;
import com.exacc.adnotatio.delivery.entity.Delivery;
import com.exacc.adnotatio.delivery.repository.DeliveryRepository;
import com.exacc.adnotatio.order.entity.Order;
import com.exacc.adnotatio.order.repository.OrderRepository;
import com.exacc.adnotatio.product.entity.Product;
import com.exacc.adnotatio.product.repository.ProductRepository;
import com.exacc.adnotatio.user.entity.Role;
import com.exacc.adnotatio.user.entity.User;
import com.exacc.adnotatio.user.entity.UserCustomer;
import com.exacc.adnotatio.user.entity.UserRole;
import com.exacc.adnotatio.user.repository.UserCustomerRepository;
import com.exacc.adnotatio.user.repository.UserRepository;
import com.exacc.adnotatio.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthorizationIntegrationTest {

    private static final String PASSWORD = "integration-password";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private UserCustomerRepository userCustomerRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Customer customer;
    private Customer otherCustomer;
    private User customerUser;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = customerRepository.save(newCustomer("Customer"));
        otherCustomer = customerRepository.save(newCustomer("Other"));
        customerUser = createUser("customer-user", Role.CUSTOMER, customer);
        createUser("other-customer", Role.CUSTOMER, otherCustomer);
        createUser("seller-user", Role.SELLER, null);
        product = productRepository.save(newProduct("Test product"));
        if (!deliveryRepository.existsById("EXD")) {
            Delivery delivery = new Delivery();
            delivery.setId("EXD");
            delivery.setName("Express Delivery");
            delivery.setCost(new BigDecimal("5000.00"));
            deliveryRepository.save(delivery);
        }
    }

    @Test
    void publicProductListAndProtectedProductDetail() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/products/{id}", product.getId())
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCanMutateOwnCartAndCheckoutButCannotUseAnotherCustomerPath() throws Exception {
        mockMvc.perform(post("/api/v1/customers/{id}/cart/items", customer.getId())
                        .with(httpBasic("customer-user", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/customers/{id}/cart/items", customer.getId())
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/customers/{id}/cart/items/{productId}",
                                customer.getId(), product.getId())
                        .with(httpBasic("customer-user", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isOk());

        Product removableProduct = productRepository.save(newProduct("Removable product"));
        mockMvc.perform(post("/api/v1/customers/{id}/cart/items", customer.getId())
                        .with(httpBasic("customer-user", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + removableProduct.getId() + ",\"quantity\":1}"))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/v1/customers/{id}/cart/items/{productId}",
                                customer.getId(), removableProduct.getId())
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/customers/{id}/orders", customer.getId())
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/customers/{id}/cart/items", otherCustomer.getId())
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sellerCanManageProductsButCannotCheckoutAndCustomerCannotManageProducts() throws Exception {
        String body = "{\"name\":\"Created by seller\",\"unitPrice\":12.50,"
                + "\"unitsInStock\":5,\"deactivated\":false}";

        mockMvc.perform(post("/api/v1/products")
                        .with(httpBasic("seller-user", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/v1/products/{id}", product.getId())
                        .with(httpBasic("seller-user", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/products/{id}", product.getId())
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/products")
                        .with(httpBasic("customer-user", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/products/{id}", product.getId())
                        .with(httpBasic("seller-user", PASSWORD)))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/customers/{id}/orders", customer.getId())
                        .with(httpBasic("seller-user", PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateAndAssignRolesButCannotManageProductsOrChangeOwnRole() throws Exception {
        String request = "{\"username\":\"new-seller\",\"password\":\"new-password\","
                + "\"role\":\"SELLER\"}";
        mockMvc.perform(post("/api/v1/users")
                        .with(httpBasic("test-admin", "test-only-admin-password"))
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());

        User target = userRepository.findByUsername("seller-user").orElseThrow();
        mockMvc.perform(patch("/api/v1/users/{id}/role", target.getId())
                        .with(httpBasic("test-admin", "test-only-admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());

        User admin = userRepository.findByUsername("test-admin").orElseThrow();
        mockMvc.perform(patch("/api/v1/users/{id}/role", admin.getId())
                        .with(httpBasic("test-admin", "test-only-admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SELLER\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/products")
                        .with(httpBasic("test-admin", "test-only-admin-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"No admin product access\",\"unitPrice\":1,"
                                + "\"unitsInStock\":1,\"deactivated\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerCannotManageRolesOrReadAnotherCustomersOrders() throws Exception {
        createUser("other-seller", Role.SELLER, null);
        Order ownOrder = createOrder(customer);
        Order otherOrder = createOrder(otherCustomer);

        mockMvc.perform(patch("/api/v1/users/{id}/role",
                                userRepository.findByUsername("other-seller").orElseThrow().getId())
                        .with(httpBasic("customer-user", PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/orders/{id}", ownOrder.getId())
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/orders")
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
        mockMvc.perform(get("/api/v1/orders/{id}", otherOrder.getId())
                        .with(httpBasic("customer-user", PASSWORD)))
                .andExpect(status().isNotFound());
    }

    private User createUser(String username, Role role, Customer linkedCustomer) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(username.equals("test-admin")
                ? "test-only-admin-password" : PASSWORD));
        User saved = userRepository.save(user);
        userRoleRepository.save(new UserRole(saved, role));
        if (role == Role.CUSTOMER) {
            userCustomerRepository.save(new UserCustomer(saved, linkedCustomer));
        }
        return saved;
    }

    private Customer newCustomer(String firstName) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName("Test");
        customer.setEmail(firstName.toLowerCase() + "@example.test");
        customer.setPhoneNumber("08123456789");
        customer.setDeleted(false);
        return customer;
    }

    private Product newProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setUnitPrice(new BigDecimal("100.00"));
        product.setUnitsInStock(10);
        product.setDeactivated(false);
        return product;
    }

    private Order createOrder(Customer owner) {
        Order order = new Order();
        order.setCustomer(owner);
        order.setDelivery(deliveryRepository.findById("EXD").orElseThrow());
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryFee(new BigDecimal("5000.00"));
        order.setTotalAmount(new BigDecimal("5100.00"));
        return orderRepository.save(order);
    }
}
