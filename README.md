# Fahasa Clone Backend

A Spring Boot-based backend application for a book e-commerce platform, that clones Fahasa. This project provides REST
APIs for
managing books, authors, publishers, categories, user accounts, authentication, and more.  
For the frontend, see [fahasa-clone-frontend](https://github.com/fahasa-clone/fahasa-clone-frontend.git).

## Overview

Fahasa Clone Backend is built with modern Java technologies including **Spring Boot**, **Spring Data JPA**,
**Jakarta EE**, and **Lombok**. It features OAuth2 authentication, JWT token support, and integration with Cloudinary
for image management.

## Features

- **User Management**: Account registration, authentication, and profile management
- **Book Management**: Create, read, update, and delete book listings with detailed information
- **Author & Publisher Management**: Manage book authors and publishers
- **Category Management**: Organize books into categories
- **OAuth2 Authentication**: Social login integration
- **JWT Token Support**: Secure API endpoints with refresh token mechanism
- **Image Management**: Cloud-based image storage via Cloudinary
- **OTP Verification**: Email-based OTP for account verification and password reset
- **Database Migrations**: Liquibase-based schema management with seed data

## Tech Stack

- **Java 17**: Latest LTS version
- **Spring Boot 3.x**: Modern application framework
- **Spring Data JPA**: Data access layer
- **Spring Security**: Authentication and authorization
- **Spring MVC**: Web application framework
- **Jakarta EE**: Enterprise Java specifications
- **Lombok**: Reduce boilerplate code
- **PostgreSQL**: Relational database
- **Liquibase**: Database schema versioning
- **Cloudinary**: Cloud image storage service
- **Maven**: Dependency management and build tool
