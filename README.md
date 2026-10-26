# CareerForge 🚀

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4%2B-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?logo=openjdk)](https://openjdk.org/)
[![React](https://img.shields.io/badge/React-19.3-blue.svg?logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-8.3-purple.svg?logo=vite)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%20InnoDB-blue.svg?logo=mysql)](https://www.mysql.com/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations%20V1--V7-red.svg?logo=flyway)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage%20Compose-2496ED.svg?logo=docker)](https://www.docker.com/)
[![Google Gemini](https://img.shields.io/badge/Google%20Gemini-1.5%20Flash-4285F4.svg?logo=google)](https://aistudio.google.com/)

> **Next-Generation, AI-Orchestrated Job & Recruitment Platform** featuring multi-dimensional candidate compatibility scoring, Google Gemini generative intelligence, real-time STOMP WebSockets, Apache PDFBox document parsing, and token-bucket rate limiting.

---

## 📌 Architecture & System Overview

```
                                  [ Browser / Client ]
                                           │
                        ┌──────────────────┴──────────────────┐
                        │      http://localhost:5173          │
                        │   (Nginx :80 in Docker Compose)     │
                        └──────────────────┬──────────────────┘
                                           │
               ┌───────────────────────────┴───────────────────────────┐
               │                                                       │
        [ HTTP / REST ]                                         [ STOMP / WebSocket ]
       /api/v1/* (Axios)                                        /ws (SockJS Fallback)
               │                                                       │
               ▼                                                       ▼
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                      CareerForge Spring Boot 3 Backend                      │
 │                                                                             │
 │  ┌───────────────────────┐ ┌──────────────────────┐ ┌────────────────────┐ │
 │  │ Bucket4j Rate Limiter │ │ Spring Security JWT  │ │ OpenAPI Swagger UI │ │
 │  └───────────────────────┘ └──────────────────────┘ └────────────────────┘ │
 │                                                                             │
 │  ┌───────────────────────────────────────────────────────────────────────┐  │
 │  │                         Application Modules                           │  │
 │  │  • Auth & User RBAC (Candidate, Recruiter, Admin)                     │  │
 │  │  • Job Lifecycle (Draft, Published, Closed) & Taxonomy Taxonomy       │  │
 │  │  • Candidate Profiles, Experience & Education History                 │  │
 │  │  • Application Pipeline & Review Progressions                         │  │
 │  │  • AI Matching Engine (60% Skills, 25% Experience, 15% Profile)       │  │
 │  │  • Real-Time Notification & Audit Logging                             │  │
 │  │  • Recruitment Analytics & KPI Dashboard                              │  │
 │  │  • Resume Storage (Apache PDFBox) & Inline Streaming                  │  │
 │  └───────────────────────────────────────────────────────────────────────┘  │
 │                                                                             │
 │  ┌───────────────────────────────────┐ ┌─────────────────────────────────┐  │
 │  │   Google Gemini 1.5 Flash AI      │ │   Local File System Resume Vault│  │
 │  │   (Job Descriptions, Resume Score)│ │   (PDF Uploads & Stream Download│  │
 │  └───────────────────────────────────┘ └─────────────────────────────────┘  │
 └──────────────────────────────────────┬──────────────────────────────────────┘
                                        │
                               [ HikariCP / JPA ]
                                        │
                                        ▼
                        ┌──────────────────────────────┐
                        │     MySQL 8.0 InnoDB Database│
                        │   Flyway Migrations (V1–V7)  │
                        └──────────────────────────────┘
```

---

## ✨ Key Capabilities

### 1. 🧠 Multi-Factor AI Matching Engine
- Multi-dimensional compatibility algorithm scoring candidate fit against job requirements:
  - **60% Skills Overlap**: Jaccard-weighted taxonomy alignment with matched vs missing skill feedback.
  - **25% Experience Depth**: Ratio of candidate career tenure to job experience requirements.
  - **15% Profile Completeness**: Bio, verified education records, and resume document availability.
- Accessible via `/api/v1/jobs/{jobId}/match` and candidate recommendation feed `/api/v1/candidate/job-matches`.

### 2. ⚡ Google Gemini Generative Intelligence
- **1-Click AI Job Description Generator**: Auto-generates comprehensive responsibilities, required qualifications, and company culture descriptions from title and key technologies.
- **Resume Intelligence Analyzer**: Parses extracted PDF text with Gemini to extract key technologies, calculate resume strength score (0–100%), and offer actionable improvement suggestions.

### 3. 📄 Apache PDFBox Document Vault
- Validates multipart PDF uploads (max 5MB), checks MIME types, prevents directory traversal, and extracts text content.
- Streams PDF documents inline directly to recruiter browsers for applicant reviews.

### 4. 🔔 Real-Time STOMP WebSockets
- Asynchronous notifications dispatched when application stages progress (`SUBMITTED` -> `SHORTLISTED` -> `INTERVIEW_SCHEDULED` -> `OFFER_EXTENDED` / `REJECTED`).
- User-specific private queues (`/user/{email}/queue/notifications`) and public broadcast announcements (`/topic/announcements`).

### 5. 🛡️ Token-Bucket Rate Limiting (Bucket4j)
- High-throughput endpoint rate limiting by IP:
  - Auth endpoints: 10 requests / minute
  - AI endpoints: 5 requests / minute
  - General API: 100 requests / minute
- Sends standard `X-Rate-Limit-Remaining` and `Retry-After` headers on HTTP 429.

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21 LTS, Spring Boot 3.4+, Spring Data JPA, Spring Security, Flyway, JJWT, Bucket4j, SpringDoc OpenAPI 3.0, Apache PDFBox |
| **Frontend** | React 19, Vite, Axios with Bearer Interceptors, TanStack Query v5, STOMP.js & SockJS, Lucide Icons, Vanilla CSS Design System |
| **Database** | MySQL 8.0 (InnoDB engine, UTF8MB4, foreign key cascade constraints, Flyway V1–V7) |
| **AI / LLM** | Google Gemini 1.5 Flash (Google AI Studio REST integration) |
| **Containerization** | Multi-stage Dockerfiles (Eclipse Temurin 21 JRE, Nginx Alpine), Docker Compose |

---

## 🚀 Quickstart Guide

### Option 1: Running Locally (Development Mode)

#### Prerequisites
- **JDK 21** (`java -version`)
- **Node.js 20+** and **npm** (`node -v`)
- **MySQL 8.0** running on `localhost:3306`

#### 1. Setup Database
Create the MySQL database:
```sql
CREATE DATABASE careerforge_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. Start Backend Server
```bash
cd backend
./mvnw spring-boot:run
```
*The Spring Boot backend will auto-migrate Flyway scripts V1 through V7 and listen on `http://localhost:8080`.*

#### 3. Start Frontend Client
```bash
cd frontend
npm install
npm run dev
```
*The Vite frontend application will start on `http://localhost:5173`.*

---

### Option 2: Running with Docker Compose (One-Command Deployment)

```bash
# 1. Copy the environment configuration template
cp .env.example .env

# 2. Build and run the entire multi-container stack
docker compose up --build
```

Services started:
- **MySQL 8 Container**: Port 3306 with healthcheck and persistent volume `mysql_data`.
- **Spring Boot Backend**: Port 8080 with auto-healing Flyway migrations.
- **Nginx & React Frontend**: Port 80 with SPA routing, reverse proxy `/api/`, and WebSocket upgrade `/ws/`.

---

## 🔑 Demo Accounts

Use these pre-seeded demo accounts on the **Login Page** or click the 1-click evaluation buttons:

| Role | Email | Password | Pre-Configured Assets |
| :--- | :--- | :--- | :--- |
| **Candidate** | `sarah.chen@example.com` | `Candidate@123` | Full engineering profile, Java/Spring/React skills, PDF resume |
| **Recruiter** | `alex.recruiter@example.com` | `Recruiter@123` | TechCorp Innovations recruiter, active distributed systems job listing |

---

## 📖 API Documentation (OpenAPI Swagger)

When the backend is running, inspect and test interactive endpoints via Swagger UI:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Primary Endpoint Map:
- **Auth**: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
- **Jobs**: `GET /api/v1/jobs`, `POST /api/v1/jobs`, `PATCH /api/v1/jobs/{id}/status`
- **Matching**: `GET /api/v1/jobs/{id}/match`, `GET /api/v1/candidate/job-matches`, `GET /api/v1/recruiter/jobs/{id}/candidate-matches`
- **Applications**: `POST /api/v1/jobs/{id}/apply`, `GET /api/v1/candidate/applications`, `PATCH /api/v1/recruiter/applications/{id}/status`
- **Profiles**: `GET /api/v1/candidate/profile`, `PUT /api/v1/candidate/profile`, `POST /api/v1/candidate/education`, `POST /api/v1/candidate/experience`
- **Resumes**: `POST /api/v1/candidate/resume/upload`, `GET /api/v1/candidate/resume/download/{fileName}`
- **AI Assist**: `POST /api/v1/ai/jobs/generate-description`, `POST /api/v1/ai/resume/analyze`
- **Notifications**: `GET /api/v1/notifications`, `GET /api/v1/notifications/unread-count`, `POST /api/v1/notifications/test-push`
- **Analytics**: `GET /api/v1/analytics/kpis`, `GET /api/v1/analytics/skills/demand-supply`

---

## 🧪 Testing Suite

### Backend Unit & Integration Tests (21 Passing Tests)
```bash
cd backend
./mvnw test
```
*Validates JWT authentication filters, Bucket4j rate limiting, AI matching engine calculation, applicant pipeline progressions, and Flyway migration integrity against in-memory H2 database.*

### Frontend Production Build
```bash
cd frontend
npm run build
```
*Validates type safety, JSX syntax, Rollup chunk minification, and CSS tokens.*

---

## 📄 License
This project is licensed under the MIT License.
