-- BharatMart Phase 3: Initial Seed Data (Indian States, Cities, Stores, Roles, Demo Users, Catalog, Inventory)

-- 1. Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'CUSTOMER', 'Standard shopper account'),
(2, 'STORE_MANAGER', 'Manages inventory, orders, and COD collections for an assigned store'),
(3, 'ADMIN', 'Platform administrator with global store, user, and middleware monitoring access');

-- 2. Demo Users (BCrypt hash for 'Password@123' - replace in production!)
INSERT INTO users (id, username, email, password_hash, account_status) VALUES
(1, 'admin', 'admin@bharatmart.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE'),
(2, 'manager_blr', 'manager.blr@bharatmart.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE'),
(3, 'aarav_sharma', 'customer@bharatmart.in', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 3), -- admin -> ADMIN
(2, 2), -- manager_blr -> STORE_MANAGER
(3, 1); -- aarav_sharma -> CUSTOMER

-- 3. States
INSERT INTO states (id, code, name) VALUES
(1, 'KA', 'Karnataka'),
(2, 'TN', 'Tamil Nadu'),
(3, 'MH', 'Maharashtra'),
(4, 'DL', 'Delhi'),
(5, 'TS', 'Telangana');

-- 4. Cities
INSERT INTO cities (id, state_id, code, name) VALUES
(1, 1, 'BLR', 'Bengaluru'),
(2, 1, 'MYS', 'Mysuru'),
(3, 2, 'CHE', 'Chennai'),
(4, 3, 'MUM', 'Mumbai'),
(5, 4, 'DEL', 'New Delhi'),
(6, 5, 'HYD', 'Hyderabad');

-- 5. Stores (India-wide unique store codes)
INSERT INTO stores (id, store_code, name, state_id, city_id, address_line, pincode, contact_phone, manager_user_id, is_active) VALUES
(1, 'IN-KA-BLR-STORE001', 'BharatMart Bengaluru Indiranagar', 1, 1, '100 Feet Road, Indiranagar', '560038', '+91-80-41110001', 2, TRUE),
(2, 'IN-KA-MYS-STORE002', 'BharatMart Mysuru Vijayanagar', 1, 2, 'Kalidasa Road, Vijayanagar', '570017', '+91-821-4111002', NULL, TRUE),
(3, 'IN-TN-CHE-STORE003', 'BharatMart Chennai T. Nagar', 2, 3, 'Usman Road, T. Nagar', '600017', '+91-44-41110003', NULL, TRUE),
(4, 'IN-MH-MUM-STORE004', 'BharatMart Mumbai Andheri West', 3, 4, 'Link Road, Andheri West', '400053', '+91-22-41110004', NULL, TRUE);

-- 6. Customer Profile & Sample Address
INSERT INTO customer_profiles (id, user_id, full_name, phone, preferred_store_id) VALUES
(1, 3, 'Aarav Sharma', '+91-9876543210', 1);

INSERT INTO addresses (id, customer_id, label, recipient_name, phone, line1, line2, city_id, state_id, pincode, is_default) VALUES
(1, 1, 'Home', 'Aarav Sharma', '+91-9876543210', '42, 12th Main Road', 'HAL 2nd Stage, Indiranagar', 1, 1, '560008', TRUE);

-- 7. Categories
INSERT INTO categories (id, name, slug, description) VALUES
(1, 'Electronics', 'electronics', 'Smartphones, audio, and everyday tech'),
(2, 'Groceries & Staples', 'groceries-staples', 'Indian kitchen essentials, rice, spices, and pulses'),
(3, 'Home & Kitchen', 'home-kitchen', 'Cookware, appliances, and home organization');

-- 8. Products
INSERT INTO products (id, category_id, name, slug, brand, description, is_active) VALUES
(1, 1, 'boAt Airdopes 141 Wireless Earbuds', 'boat-airdopes-141', 'boAt', '42H playback, ENx tech, IPX4 water resistance', TRUE),
(2, 2, 'India Gate Classic Basmati Rice', 'india-gate-classic-basmati', 'India Gate', 'Aged extra-long grain basmati rice', TRUE),
(3, 3, 'Prestige Svachh Pressure Cooker', 'prestige-svachh-cooker', 'Prestige', 'Deep lid stainless steel pressure cooker with anti-spillage control', TRUE);

-- 9. SKUs
INSERT INTO skus (id, product_id, sku_code, variant_name, mrp, selling_price, is_active) VALUES
(1, 1, 'SKU-ELEC-1001', 'Bold Black', 4490.00, 1299.00, TRUE),
(2, 1, 'SKU-ELEC-1002', 'Pure White', 4490.00, 1299.00, TRUE),
(3, 2, 'SKU-GROC-2001', '5 kg Pack', 1150.00, 949.00, TRUE),
(4, 3, 'SKU-HOME-3001', '3 Litre Stainless Steel', 2695.00, 2199.00, TRUE);

-- 10. Store-Wise Inventory (Notice different stock levels across India!)
INSERT INTO inventory (store_id, sku_id, available_qty, reserved_qty, reorder_level) VALUES
(1, 1, 45, 0, 10), -- BLR: Bold Black Earbuds
(1, 2, 6, 0, 10),  -- BLR: Pure White Earbuds (Low stock alert!)
(1, 3, 120, 0, 20),
(1, 4, 30, 0, 8),
(2, 1, 18, 0, 10), -- MYS
(2, 3, 80, 0, 20),
(3, 1, 60, 0, 10), -- CHE
(3, 4, 22, 0, 8),
(4, 1, 75, 0, 10), -- MUM
(4, 2, 40, 0, 10),
(4, 3, 95, 0, 20);
