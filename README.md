# Overlook Hotel

Welcome to **Overlook Hotel**, a Spring Boot–based hotel management application that covers everything from room booking to employee scheduling, feedback, loyalty points, events, analytics and more. This project will deepen your Java and Spring expertise while simulating a real‑world hospitality management system.


## Features

1. **Authentication & Roles**  
   - Separate login pages for **Clients** and **Employee**  
   - Role‐based access control (CLIENT vs. ADMIN vs. EMPLOYEE)  

2. **Room Management**  
   - CRUD API for rooms (add, edit, delete, list)  
   - EMPLOYEE UI for room inventory  
   - Client UI for browsing and booking available rooms  

3. **Reservation Management**  
   - CRUD API for reservations (create, view, modify, cancel)  
   - Employee view of all bookings with edit/cancel options  
   - Client view: upcoming & past reservations; new booking interface  

4. **Client & Employee Management**  
   - CRUD API for client and employee records  
   - Employee UI for personnel data  

5. **Feedback & Ratings**  
   - API for clients to submit comments and star–ratings  
   - Client page to leave feedback  
   - Employee dashboard to review and respond to feedback  

6. **Loyalty Program**  
   - Points‐based system rewarding repeat clients  
   - Client UI to view and redeem loyalty points  

7. **Events & Facilities**  
   - API to create/manage hotel events and facility bookings  
   - Automated notification system (email/webhook)  
   - Employee UI to schedule events & configure notifications  
   - Client UI to browse upcoming events and reserve facilities  

8. **Analytics & Reporting**  
   - Backend tools for occupancy rates, financial KPIs, and satisfaction metrics  
   - Dashboards for both clients and managers  

9. **Advanced Employee Management**  
   - API for shift scheduling, leave requests, training records  
   - Dedicated UI for HR tasks (managers and employees)  

---

## Prerequisites

- **Java 17+** (JDK)  
- **Maven 3.6+** _or_ **Gradle 7+**  
- **MySQL 8+** (or another JDBC‑compatible relational DB)  
- **Git** for version control  

---

## Getting Started

1. **Clone the repository**  
   ```bash
   git clone https://github.com/<your‑username>/overlook_hotel.git
   cd overlook_hotel

---

## Project Structure

```text
overlook_hotel/
├── .gitignore
├── README.md
└── master/
    ├── .gitignore
    ├── HELP.md
    ├── mvnw
    ├── mvnw.cmd
    ├── pom.xml
    ├── src/
    │   └── main/
    │       ├── java/
    │       │   └── master/master/
    │       │       ├── MasterApplication.java
    │       │       ├── config/         ← Spring configuration classes (SecurityConfig, etc.)
    │       │       ├── domain/         ← JPA entities
    │       │       ├── dto/            ← Data Transfer Objects
    │       │       ├── exceptions/     ← Custom exception handlers
    │       │       ├── mapper/         ← MapStruct mappers
    │       │       ├── repository/     ← Spring Data JPA repositories
    │       │       ├── service/        ← Business logic
    │       │       ├── security/       ← JwtUtil
    │       │       └── web/            ← MVC & REST controllers
    │       └── resources/
    │           ├── application.yml     ← Spring Boot configuration
    │           ├── static/
    │           │   ├── css/
    │           │   │   ├── employeeDashboard.css
    │           │   │   └── homeLoginPage.css
    │           │   ├── js/
    │           │   └── images/
    │           └── templates/
    │               ├── clientLoginPage.html
    │               ├── employeeDashboard.html
    │               ├── employeeLoginPage.html
    │               ├── homeLoginPage.html
    │               ├── registerPage.html
    │               └── roomManagement.html


