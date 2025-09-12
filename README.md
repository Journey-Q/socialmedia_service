# 📱 SocialMedia Service

A backend **microservice** that powers core social media features — user posts, comments, likes, and following relationships.  
It is designed to be part of a larger **microservices architecture**.

---

## ✨ Features

- 📝 Create, edit, delete posts  
- 💬 Commenting system for posts  
- ❤️ Like / unlike posts  
- 👥 User profile & follower/following management  
- 📰 Timeline feed with posts from followed users  

---

socialmedia_service/
├── .mvn/                       # Maven wrapper
├── src/
│   ├── main/
│   │   ├── java/com/journeyq/socialmedia_service/
│   │   │   ├── controller/     # REST controllers (API endpoints)
│   │   │   ├── service/        # Business logic
│   │   │   ├── model/          # Entities / domain models
│   │   │   ├── repository/     # Data persistence (JPA repositories)
│   │   │   └── config/         # Configurations (security, app configs)
│   │   └── resources/
│   │       ├── application.properties  # Database & app settings
│   │       └── static/                 # Optional static resources
│   └── test/java/com/journeyq/socialmedia_service/
│           └── ... tests for services & controllers
├── pom.xml                     # Maven dependencies & build config
├── mvnw / mvnw.cmd             # Maven wrapper scripts
└── .gitignore


## 🛠 Tech Stack

| Component         | Technology |
|-------------------|------------|
| Language          | Java |
| Framework         | Spring Boot |
| Build Tool        | Maven |
| Database          | MySQL / PostgreSQL |
| Authentication    | JWT-based authentication | Spring Security
| API Style         | RESTful APIs |

