# SmartBank 🏦

[![Java](https://img.shields.io/badge/Java-17-blue?logo=java&logoColor=white)](https://www.java.com/) 
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/) 
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

**SmartBank** is a console-based banking system built in Java with PostgreSQL as the backend.  
It allows users to manage accounts, perform transactions, and keep track of balances in a secure and easy-to-use way.

---

## Features ✨

- **User Management:** Register, login, and delete users.
- **Account Operations:** Deposit, withdraw, transfer, and check balance.
- **Transaction Logging:** All transactions are stored for record-keeping.
- **Database Integration:** Uses PostgreSQL with JDBC for persistent storage.

---

## Quick Start ⚡

## Setup Instructions for SmartBankApp

1️⃣ **Clone the Repository**
$ git clone https://github.com/Abhishek-B-05/Smart-Bank.git
$ cd "Smart-Bank"

2️⃣ **Create Your `.env` File**
$ cp .env.example .env

Open `.env` and verify the dev user credentials:

DB_URL=jdbc:postgresql://main-smart-bank.d.aivencloud.com:13580/defaultdb?ssl=require
DB_USER=dev_user
DB_PASS=DevUserPassword123

> ⚠️ Note: `.env` is ignored by Git, so your credentials remain private.

3️⃣ **Database Setup**
- Ensure PostgreSQL is running.
- The dev user has **limited access**: can read/write tables but cannot alter DB settings or drop tables.
- Use provided SQL scripts (if any) to create tables or initial data.

4️⃣ **Run the Application**
- Open the project in IntelliJ.
- Run the main class: `SmartBankApp.java`

The application will read the `.env` file automatically and connect to the database.

5️⃣ **Optional: Use Environment Variables**
Instead of `.env`, you can set environment variables:

**Windows (PowerShell):**
$ setx DB_URL "jdbc:postgresql://..."
$ setx DB_USER "dev_user"
$ setx DB_PASS "DevUserPassword123"

**Linux/macOS (bash):**
$ export DB_URL="jdbc:postgresql://..."
$ export DB_USER="dev_user"
$ export DB_PASS="DevUserPassword123"

The application will use these if `.env` is not present.


---

## Usage 📝

1. **Register** a new user  
2. **Login** using email and password  
3. Perform transactions:
   - Deposit
   - Withdraw
   - Transfer
   - Show Balance  
4. **Delete account** if needed (permanent)  
5. **Logout**  


---

## Contributing 🤝

1. Fork the repo  
2. Create your feature branch (`git checkout -b feature-name`)  
3. Commit your changes (`git commit -m 'Add feature'`)  
4. Push to the branch (`git push origin feature-name`)  
5. Open a pull request  

---

## License 📄

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Contact ✉️

Created by **Abhishek B**  
- GitHub: [https://github.com/<your-username>](https://github.com/<your-username>)
