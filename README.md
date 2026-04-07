# Broker Client Management System

## Overview
This is a web-based application designed for insurance brokers to manage customers, policies, and tasks through a centralized dashboard.  
The system allows efficient tracking of customer activity, policy details, and pending work.

---

## Features
- Customer management (add, edit, view, delete)
- Policy management (type, billing, payment status, expiry tracking)
- Advanced policy details (Auto and Home policies)
- Co-insured management
- Notes system for customers
- To-do / follow-up tracking
- Dashboard (summary, monitoring, reporting)
- Authentication system (login + basic 2FA simulation)

---

## Requirements
- Java (JDK 17 or higher)
- Maven (wrapper included in project)
- Any IDE (VS Code recommended)

--- 

## How to Run

1. Clone the repository:
  git clone https://github.com/mohaz118/group6-cms

2. Navigate into the project folder:
  cd group6-cms

3. Run the application:
  .\mvnw.cmd spring-boot:run

4. Open the application in your browser:
  http://localhost:8080/


---

## Test Account

If no account exists, create one using the signup page.

Example credentials:
- Email: admin@gmail.com  
- Password: admin123  

---

## Notes
- The application uses an H2 in-memory database.
- The database is automatically created when the application starts.
- No external database setup is required.
- All data is stored locally during runtime.

---

## Submission Info
- GitHub Repository: https://github.com/mohaz118/group6-cms


