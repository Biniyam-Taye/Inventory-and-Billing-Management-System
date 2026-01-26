# Inventory and Billing Management System

## Prerequisites
1. **WAMP Server**: Ensure WAMP is running.
   - MySQL Port: 3306
   - User: `root`
   - Password: `` (empty)
   - *Note: The system will automatically create the `inventory_db` database.*

2. **Java Development Kit (JDK)**: Version 17 or higher recommended.
3. **Apache NetBeans**: Any recent version.

## Project Setup in Apache NetBeans
1. Open Apache NetBeans.
2. Go to **File > Open Project**.
3. Navigate to this folder: `c:\Users\biniy\OneDrive\Desktop\java_final`.
4. Determine if it is recognized as a Maven Project (Icon usually has an 'M'). Open it.
5. NetBeans will download necessary dependencies (JavaFX, MySQL Connector) automatically.

## How to Run

### Step 1: Start the Server
The server handles the database connection and synchronization.
1. In NetBeans, expand the **Source Packages**.
2. Navigate to `com.inventory.server`.
3. Right-click on `InventoryServer.java` and select **Run File**.
4. You should see output: `Inventory Server Ready.` in the console.

### Step 2: Start the Client (GUI)
1. Navigate to `com.inventory.client`.
2. Right-click on `ClientApp.java` and select **Run File**.
3. The **Login Screen** will appear first.
4. You can open multiple clients (run `ClientApp.java` again) to test real-time updates.

## Authentication
The system now includes user authentication with beautiful, modern UI:

### First Time Users
1. Click **"Sign Up"** on the login screen
2. Fill in your details:
   - Full Name
   - Username (must be unique)
   - Email (must be unique and valid)
   - Password (minimum 6 characters)
   - Confirm Password
3. Click **"Create Account"**
4. You'll be redirected to the login screen

### Existing Users
1. Enter your **Username** and **Password**
2. Click **"Sign In"**
3. Access granted to the main inventory system

## Features Usage
1. **Inventory Tab**: Add products first.
2. **Billing Tab**: Enter the Product ID and Quantity to sell.
3. **Invoices**: Invoices are generated as text files in the project root folder.
4. **Real-time**: If you have two clients open, adding a product or selling in one will update the logs in the other via Sockets.

## Configuration
If your MySQL password is not empty, edit:
`src/main/java/com/inventory/server/DatabaseHandler.java`
Update the `USER` and `PASS` constants.

