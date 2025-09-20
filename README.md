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

1️⃣ **Clone the Repository**

```bash
$ git clone https://github.com/Abhishek-B-05/Smart-Bank.git

$ cd "Smart-Bank"
```

2️⃣ **Use the Provided Dev `.env` File**
- The repository includes a safe `banking.env` for the dev user.
- Copy it to `.env` in the project root:

```bash
$ cp banking.env .env
```

3️⃣ **Database Setup**
- Ensure PostgreSQL is running.
- The dev user has limited access: can read/write tables but cannot alter DB settings or drop tables.
- Use provided SQL scripts (if any) to create tables or initial data.

4️⃣ **Run the Application**
- Open the project in IntelliJ.
- Run the main class: `SmartBankApp.java`

The application will read the `.env` file automatically and connect to the database.

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
