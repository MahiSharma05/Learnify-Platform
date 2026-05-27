# 🎓 Learnify Platform

Learnify Platform is a modern E-Learning Management System (LMS) built using a Microservices Architecture. It enables students to enroll in courses, instructors to manage educational content, and administrators to monitor the platform efficiently.

---

## 🚀 Features

### 👨‍🎓 Student Features
- User Registration & Login
- Browse Available Courses
- Enroll in Courses
- Access Course Content
- Attempt Quizzes
- Track Learning Progress
- View Course Details

### 👨‍🏫 Instructor Features
- Create and Manage Courses
- Upload Learning Materials
- Create Quizzes and Assessments
- Monitor Student Progress
- Update Course Information

### 👨‍💼 Admin Features
- Manage Users
- Manage Courses
- Monitor Platform Activities
- View Reports and Analytics

---

## 🏗️ Microservices Architecture

The application follows a distributed microservices architecture.

### Services

| Service | Description |
|----------|-------------|
| Discovery Server | Service Registration & Discovery using Eureka |
| API Gateway | Single Entry Point for Client Requests |
| Auth Service | Authentication & Authorization |
| User Service | User Management |
| Course Service | Course Management |
| Enrollment Service | Course Enrollment Management |
| Quiz Service | Quiz and Assessment Management |
| Notification Service | Email and Event Notifications |

---

## 🛠️ Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Cloud
- Eureka Server
- OpenFeign
- RabbitMQ
- Redis Cache
- JWT Authentication
- Maven

### Frontend
- Angular
- TypeScript
- HTML5
- CSS3
- Bootstrap

### Database
- MySQL

### DevOps & Deployment
- Docker
- Docker Compose

---

## 🔐 Security

- JWT Based Authentication
- Role Based Access Control (RBAC)
- Secure API Endpoints
- Password Encryption using BCrypt

---

## 📦 Project Structure

```text
Learnify-Platform
│
├── discovery-server
├── api-gateway
├── auth-service
├── user-service
├── course-service
├── enrollment-service
├── quiz-service
├── notification-service
│
├── frontend-angular
│
├── docker-compose.yml
└── README.md
```

---

## 🔄 Communication Flow

1. User sends request from Angular Frontend.
2. Request reaches API Gateway.
3. Gateway routes request to appropriate Microservice.
4. Services discover each other using Eureka Server.
5. Inter-service communication happens using OpenFeign.
6. Events are processed asynchronously using RabbitMQ.
7. Frequently accessed data is cached using Redis.
8. Data is stored in MySQL Database.
9. Response is returned to the Frontend.

---


## 🐳 Docker

The project uses Docker Compose for container orchestration.

Services included:

- Eureka Server
- API Gateway
- MySQL
- Redis
- RabbitMQ
- Backend Microservices
- Angular Frontend

Run:

```bash
docker-compose up --build
```

---

## 📚 API Documentation

API documentation can be accessed through Swagger UI after starting the services.

```text
http://localhost:8080/swagger-ui.html
```

---

## 📈 Future Enhancements

- AI-Based Course Recommendations
- Real-Time Chat System
- Mobile Application Support

---

## ⭐ Support

If you found this project useful, please give it a ⭐ on GitHub and share your feedback.

---
