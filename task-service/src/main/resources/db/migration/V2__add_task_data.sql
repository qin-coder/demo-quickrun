
INSERT INTO tasks (name, description, base_fee, per_km_rate, active, created_at, updated_at)
VALUES
-- Basic delivery services
('Express Delivery',
 'Pick up and deliver small parcels or items within the same city. Ideal for fast delivery.',
 3.00, 1.00, TRUE, '2024-01-01 09:00:00', '2024-01-02 10:30:00'),

('Help Buy',
 'Purchase items from nearby stores and deliver them to customers quickly.',
 2.50, 0.80, TRUE, '2024-01-02 10:00:00', '2024-01-03 11:15:00'),

('Document Pickup',
 'Pickup and deliver important documents securely between offices or individuals.',
 2.00, 0.70, TRUE, '2024-01-03 09:30:00', '2024-01-04 09:45:00'),

('Food Delivery',
 'Deliver restaurant meals to customers using real-time driver tracking.',
 2.80, 0.90, TRUE, '2024-01-04 08:00:00', '2024-01-05 12:10:00'),

('Grocery Shopping',
 'Assist customers with grocery shopping and deliver to their doorstep.',
 3.20, 0.85, TRUE, '2024-01-05 09:00:00', '2024-01-06 10:00:00'),

('Pharmacy Delivery',
 'Deliver prescribed medicines safely and quickly from local pharmacies.',
 2.50, 0.75, TRUE, '2024-01-06 08:30:00', '2024-01-07 09:45:00'),

('Large Parcel Delivery',
 'Deliver heavier or larger items using specialized vehicles.',
 5.00, 1.50, TRUE, '2024-01-07 07:30:00', '2024-01-08 08:40:00'),

('Night Express',
 'Late-night delivery service with premium charge for speed and availability.',
 4.00, 1.20, TRUE, '2024-01-08 22:00:00', '2024-01-09 23:10:00');
