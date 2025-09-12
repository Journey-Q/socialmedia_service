# 📱 SocialMedia Service

A backend **microservice** that powers core social media features — user posts, comments, likes, and following relationships.  
It is designed to be part of a larger **event-driven microservices architecture**.

---

## ✨ Features

- 📝 Create, edit, delete posts  
- 💬 Commenting system for posts  
- ❤️ Like / unlike posts  
- 👥 User profile & follower/following management  
- 📰 Timeline feed with posts from followed users  

---

## 🏗️ Architecture

- **Microservices-based** → Each core feature (auth, social, notifications, chat, etc.) is its own service.  
- **Event-driven communication** → Services communicate asynchronously using **Apache Kafka**.  
- **Pub/Sub model** → Events (e.g., *PostCreated*, *UserFollowed*) are published to Kafka topics, and other services subscribe to react (e.g., Notification service sends alerts).  


---

## 🛠 Tech Stack

| Component         | Technology |
|-------------------|------------|
| Language          | Java |
| Framework         | Spring Boot |
| Build Tool        | Maven |
| Database          | MySQL / PostgreSQL |
| Authentication    | Spring Security (JWT-based) |
| Messaging / PubSub| Apache Kafka |
| API Style         | RESTful APIs |

---

## 📌 Short Description

**socialmedia_service** is a **Spring Boot microservice** that manages social media features like posts, comments, likes, and following.  
It works in an **event-driven ecosystem** where actions (e.g., creating a post, following a user) are published as **Kafka events**, enabling other services (e.g., notifications, analytics, recommendations) to subscribe and react asynchronously.  

---
