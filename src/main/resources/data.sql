
INSERT INTO products (name, price, stock, deactivated)
VALUES
    ('Keyboard', 400000, 9, FALSE),
    ('Mouse', 100000, 9, FALSE),
    ('Monitor', 100000, 5, FALSE),
    ('Laptop', 7000000, 3, FALSE),
    ('Pearbook Mini', 10000000, 2, TRUE);


INSERT INTO Customers (first_name, last_name, phone_number, email, is_deleted)
VALUES
    ('Alice', 'Johnson', '08123456781', 'alice@gmail.com', FALSE),
    ('Ben', 'Kornel', '08123456782', 'ben@gmail.com', FALSE),
    ('Charles', 'Lee', '08123456783', 'charles@gmail.com', FALSE);


INSERT INTO Regions (Name)
VALUES
    ('North'),
    ('East'),
    ('West'),
    ('South');

INSERT INTO Deliveries (Id, Name, Cost)
VALUES
    ('EXD', 'Express Delivery', 5000),
    ('NOD', 'North Delivery', 10000);

INSERT INTO Orders (OrderDate, ArrivalDate, CustomerId, DeliveryId, DeliveryFee, TotalAmount)
VALUES
    ('2020-01-03', '2020-01-01', 1, 'EXD', 5000, 500000);

INSERT INTO OrderDetails (OrderId, ProductId, Price, Quantity)
VALUES
    (1, 1, 400000, 1),
    (1, 2, 100000, 1);
