insert into users (id, username, password_hash, email, phone, role, created_at) values
  (1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9e58fd6b2b224eac5bf5ec8b1e6', 'admin@example.com', '13800000000', 'ADMIN', current_timestamp),
  (2, 'alice', '2bd806c97f0e00af1a1fc3328fa763a9269723c8db8fac4f93af71db186d6e90', 'alice@example.com', '13900000000', 'CUSTOMER', current_timestamp);

insert into product_categories (id, name, description) values
  (1, 'Digital', 'Phones, tablets, and accessories'),
  (2, 'Daily', 'Useful daily goods');

insert into products (id, category_id, name, description, price, stock, status, created_at, updated_at) values
  (1, 1, 'Spring Phone', 'A demo phone for order practice', 1999.00, 50, 'ON_SHELF', current_timestamp, current_timestamp),
  (2, 1, 'Redis Earbuds', 'Shows cart cache and stock lock examples', 299.00, 120, 'ON_SHELF', current_timestamp, current_timestamp),
  (3, 2, 'Rocket Mug', 'A mug with async order event demo', 49.90, 300, 'ON_SHELF', current_timestamp, current_timestamp);
