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

### 1. Clone the repository
```bash
git clone https://github.com/<your-username>/smart-bank.git
cd smart-bank
```

### 2. Setup PostgreSQL
- Create a database (e.g., `smartbank`)  
- Create required tables: `users`, `customers`, `accounts`, `transactions`  
- Update DB credentials in `SmartBankApp.java`:
```java
private static final String DB_URL = "jdbc:postgresql://<host>:<port>/<database>?ssl=require";
private static final String DB_USER = "<username>";
private static final String DB_PASS = "<password>";
```

### 3. Run the application
```bash
javac SmartBankApp.java
java SmartBankApp
```

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
