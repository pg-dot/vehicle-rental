-- VEHICLE RENTAL — DATABASE SCHEMA

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE
);

-- Vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    plate_no VARCHAR(20) UNIQUE NOT NULL,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    daily_rate NUMERIC(10,2) NOT NULL CHECK (daily_rate >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
    -- AVAILABLE | RENTED | MAINTENANCE
);

-- Rentals table
CREATE TABLE IF NOT EXISTS rentals (
    id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    vehicle_id INT NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    base_cost NUMERIC(10,2) NOT NULL DEFAULT 0,
    late_fee NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_cost NUMERIC(10,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
    -- OPEN | RETURNED | CANCELED
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_rentals_customer ON rentals(customer_id);
CREATE INDEX IF NOT EXISTS idx_rentals_vehicle ON rentals(vehicle_id);

-- Seed data (so you have something to test right away)
INSERT INTO customers(name, phone, email) VALUES
('Asha Kulkarni','9998887770','asha@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO vehicles(plate_no, brand, model, daily_rate, status) VALUES
('MH12AB1234','Maruti','Swift',1200,'AVAILABLE'),
('MH14XY4321','Hyundai','i20',1500,'AVAILABLE'),
('MH01TT9876','Honda','City',2200,'MAINTENANCE')
ON CONFLICT DO NOTHING;

