INSERT INTO products (id, name, price, stock, deactivated)
VALUES
    (1, 'Keyboard', 400000, 9, FALSE),
    (2, 'Mouse', 100000, 9, FALSE),
    (3, 'Monitor', 100000, 5, FALSE),
    (4, 'Laptop', 7000000, 3, FALSE),
    (5, 'Pearbook Mini', 10000000, 2, TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO customers (id, first_name, last_name, phone_number, email, is_deleted)
VALUES
    (1, 'Alice', 'Johnson', '08123456781', 'alice@gmail.com', FALSE),
    (2, 'Ben', 'Kornel', '08123456782', 'ben@gmail.com', FALSE),
    (3, 'Charles', 'Lee', '08123456783', 'charles@gmail.com', FALSE)
ON CONFLICT (id) DO NOTHING;

-- Development-only seller account. Password is BCrypt for SellerDemo123!.
INSERT INTO users (username, password)
VALUES ('seller', '$2a$10$Xv0eWLawbz5b18Iox.Lo7eH0BacfaZr4/.0qvhVEHMHs9s6tnaFJy')
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'SELLER'
FROM users
WHERE username = 'seller'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO regions (id, name)
VALUES
    (1, 'North'),
    (2, 'East'),
    (3, 'West'),
    (4, 'South')
ON CONFLICT (id) DO NOTHING;

INSERT INTO deliveries (id, name, cost)
VALUES
    ('EXD', 'Express Delivery', 5000),
    ('NOD', 'North Delivery', 10000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO orders (id, order_date, arrival_date, customer_id, delivery_id, delivery_fee, total_amount)
VALUES (1, '2020-01-03', '2020-01-01', 1, 'EXD', 5000, 505000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO order_details (order_id, product_id, product_name, unit_price, quantity)
VALUES
    (1, 1, 'Keyboard', 400000, 1),
    (1, 2, 'Mouse', 100000, 1)
ON CONFLICT (order_id, product_id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('products', 'id'), (SELECT MAX(id) FROM products));
SELECT setval(pg_get_serial_sequence('customers', 'id'), (SELECT MAX(id) FROM customers));
SELECT setval(pg_get_serial_sequence('regions', 'id'), (SELECT MAX(id) FROM regions));
SELECT setval(pg_get_serial_sequence('orders', 'id'), (SELECT MAX(id) FROM orders));
