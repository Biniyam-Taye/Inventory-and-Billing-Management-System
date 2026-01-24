-- Run this in phpMyAdmin or MySQL Workbench to clear all data and reset IDs
USE inventory_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sales;
TRUNCATE TABLE products;
SET FOREIGN_KEY_CHECKS = 1;

-- After running this, your next product will start at ID 1
