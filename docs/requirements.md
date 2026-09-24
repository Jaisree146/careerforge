# CareerForge — AI-Powered Job & Placement Platform

**Document:** Software Requirements Specification (SRS)  
**Version:** 1.0  
**Date:** September 24, 2026  
**Project Type:** Full-Stack, Microservices-Based, AI-Enabled Recruitment Platform

---

# 1. Introduction

## 1.1 Project Overview

CareerForge is an AI-powered job and recruitment platform designed to connect job seekers with recruiters and companies through a centralized platform.

The system will allow candidates to create professional profiles, upload resumes, search and apply for jobs, track applications, identify skill gaps, and receive AI-assisted career guidance.

Recruiters will be able to create company profiles, publish job opportunities, manage applications, search for suitable candidates, and manage the recruitment process.

The platform will be developed as a **microservices-based application using Java and Spring Boot**, with supporting technologies for databases, caching, asynchronous communication, AI/LLM integration, containerization, CI/CD, cloud deployment, and observability.

---

# 2. Problem Statement

The current recruitment process is often fragmented across multiple platforms.

Candidates may need to:

- Maintain resumes separately.
- Search for jobs across multiple platforms.
- Determine manually whether their skills match a job.
- Track applications independently.
- Identify missing skills for a target role.
- Prepare for interviews without personalized guidance.

Recruiters may need to:

- Publish jobs.
- Review large numbers of applications.
- Search through candidate profiles.
- Compare candidate skills with job requirements.
- Manage application status manually.

CareerForge aims to provide a centralized platform that simplifies these workflows and uses AI to provide intelligent assistance while keeping the actual recruitment decisions under human control.

---

# 3. Objectives

The primary objectives of CareerForge are:

1. Provide a centralized job discovery and application platform.
2. Allow recruiters to publish and manage job opportunities.
3. Allow candidates to maintain structured professional profiles.
4. Provide secure authentication and role-based authorization.
5. Provide efficient job search and filtering.
6. Manage the complete job application lifecycle.
7. Extract useful information from candidate resumes.
8. Provide AI-assisted job matching.
9. Identify potential skill gaps between candidates and target jobs.
10. Provide an AI-powered career assistant.
11. Provide AI-assisted interview preparation.
12. Demonstrate microservice architecture and distributed-system concepts.
13. Demonstrate cloud-native deployment and DevOps practices.
14. Provide monitoring, logging, and observability.
15. Design the system so individual components can scale independently.

---

# 4. Scope

## 4.1 In Scope

The initial system will include:

- User registration and authentication
- Role-based authorization
- Candidate profiles
- Recruiter profiles
- Company profiles
- Skills management
- Education management
- Experience management
- Resume upload and processing
- Job creation and management
- Job search and filtering
- Job applications
- Application status tracking
- Saved jobs
- Notifications
- Candidate search
- AI resume analysis
- AI-assisted job matching
- Skill-gap analysis
- AI career assistant
- AI interview practice
- RAG-based information retrieval
- Caching
- Event-driven processing
- Automated testing
- Containerization
- CI/CD
- Cloud deployment
- Logging and monitoring


## 4.2 Out of Scope for Initial Version

The following will not be implemented initially:

- Real financial/payment processing
- Payroll management
- Video interview infrastructure
- Background verification
- Real-time video communication
- Direct integration with external job portals
- Automated hiring decisions
- Fully autonomous AI recruitment decisions

These may be considered for future versions.

---

# 5. User Roles

The system will initially support three primary roles.

## 5.1 Candidate

A candidate is a user looking for employment opportunities.

The candidate can:

- Register and authenticate.
- Create and update a professional profile.
- Add skills.
- Add education details.
- Add work experience.
- Upload and manage resumes.
- Search and filter jobs.
- View job details.
- Save jobs.
- Apply for jobs.
- Track applications.
- Receive notifications.
- Use AI resume analysis.
- View job-match insights.
- View potential skill gaps.
- Use the AI career assistant.
- Practice interviews using AI.

---

## 5.2 Recruiter

A recruiter represents a company and manages job opportunities.

The recruiter can:

- Register and authenticate.
- Create and manage a company profile.
- Create job postings.
- Update job postings.
- Publish and close job postings.
- View applications.
- Search candidates.
- View candidate profiles.
- Shortlist candidates.
- Reject candidates.
- Update application status.
- Receive relevant notifications.

---

## 5.3 Administrator

The administrator manages the platform.

The administrator can:

- Manage users.
- Manage recruiters.
- Manage companies.
- Manage job postings.
- Handle reported content.
- Monitor platform activity.
- Manage selected platform configurations.

---

# 6. Functional Requirements


## 6.1 Authentication and Authorization

### FR-AUTH-001 — User Registration

The system shall allow users to create an account using required registration information.

### FR-AUTH-002 — User Login

The system shall authenticate registered users using their credentials.

### FR-AUTH-003 — Secure Password Storage

The system shall never store user passwords in plain text.

### FR-AUTH-004 — Logout

The system shall allow authenticated users to log out.

### FR-AUTH-005 — Role-Based Authorization

The system shall restrict resources and operations according to the authenticated user's role.

### FR-AUTH-006 — Protected Resources

The system shall prevent unauthenticated users from accessing protected resources.

### FR-AUTH-007 — Token Management

The system shall securely manage authentication and authorization tokens.

---

# 7. Candidate Management

### FR-CAN-001 — Candidate Profile

The system shall allow candidates to create and update their professional profile.

### FR-CAN-002 — Skills

The system shall allow candidates to add, update, and remove skills.

### FR-CAN-003 — Education

The system shall allow candidates to maintain their educational qualifications.

### FR-CAN-004 — Experience

The system shall allow candidates to maintain their professional experience.

### FR-CAN-005 — Resume Upload

The system shall allow candidates to upload resumes in supported file formats.

### FR-CAN-006 — Resume Management

Candidates shall be able to view, replace, and manage their uploaded resumes.

---

# 8. Company and Recruiter Management

### FR-REC-001 — Recruiter Profile

The system shall allow recruiters to create and update recruiter profiles.

### FR-REC-002 — Company Profile

The system shall allow recruiters to create and manage company information.

### FR-REC-003 — Company Association

A recruiter shall be associated with an authorized company.

### FR-REC-004 — Recruiter Authorization

Only authorized recruiters shall be allowed to create and manage job postings associated with their company.

---

# 9. Job Management

### FR-JOB-001 — Create Job

The system shall allow authorized recruiters to create job postings.

A job posting may contain:

- Job title
- Description
- Company
- Location
- Employment type
- Experience requirements
- Required skills
- Preferred skills
- Salary information where applicable
- Application deadline

### FR-JOB-002 — Update Job

Recruiters shall be able to update their job postings.

### FR-JOB-003 — Publish Job

Recruiters shall be able to publish valid job postings.

### FR-JOB-004 — Close Job

Recruiters shall be able to close job postings.

### FR-JOB-005 — Job Search

Candidates shall be able to search available jobs.

### FR-JOB-006 — Job Filtering

Candidates shall be able to filter jobs using supported attributes such as:

- Location
- Skills
- Employment type
- Experience
- Company
- Other supported criteria

### FR-JOB-007 — Job Details

Candidates shall be able to view complete details of a job.

---

# 10. Application Management

### FR-APP-001 — Apply for Job

An authenticated candidate shall be able to apply for an active job.

### FR-APP-002 — Duplicate Application Prevention

The system shall prevent a candidate from submitting multiple active applications for the same job.

### FR-APP-003 — Application Tracking

Candidates shall be able to view the status of their applications.

### FR-APP-004 — Recruiter Application View

Recruiters shall be able to view applications submitted to their job postings.

### FR-APP-005 — Application Status

Recruiters shall be able to update application status.

### FR-APP-006 — Status History

The system shall maintain a history of relevant application status changes.

### FR-APP-007 — Candidate Shortlisting

Recruiters shall be able to shortlist candidates.

### FR-APP-008 — Candidate Rejection

Recruiters shall be able to reject candidates.

---

# 11. Saved Jobs

### FR-SAVE-001

Candidates shall be able to save jobs for later reference.

### FR-SAVE-002

Candidates shall be able to remove saved jobs.

### FR-SAVE-003

The system shall prevent duplicate saved-job records for the same candidate and job.

---

# 12. Notification Management

### FR-NOT-001

The system shall generate notifications for important application events.

### FR-NOT-002

Candidates shall receive relevant application status notifications.

### FR-NOT-003

Recruiters shall receive relevant application-related notifications.

### FR-NOT-004

Notification processing should support asynchronous execution.

### FR-NOT-005

The system shall maintain notification status where applicable.

---

# 13. AI and LLM Requirements

AI features are intended to provide assistance and insights rather than make autonomous employment decisions.

---

## 13.1 Resume Analysis

### FR-AI-001

The system shall process supported resume documents.

### FR-AI-002

The system shall extract relevant structured information from resumes.

Potential information includes:

- Skills
- Education
- Experience
- Projects
- Certifications

### FR-AI-003

The system shall allow candidates to review AI-extracted information before it is used as profile information where appropriate.

---

# 14. AI Job Matching

### FR-AI-010

The system shall compare candidate profile information with job requirements.

### FR-AI-011

The system shall identify skills that appear to match job requirements.

### FR-AI-012

The system shall identify potentially missing or insufficiently represented skills.

### FR-AI-013

The system may use semantic similarity techniques such as embeddings to improve matching.

### FR-AI-014

The system shall provide explanations or supporting information for AI-generated matching insights where feasible.

### FR-AI-015

AI-generated matching information shall not automatically determine hiring decisions.

---

# 15. Skill Gap Analysis

### FR-AI-020

The system shall allow a candidate to select or provide a target job/role.

### FR-AI-021

The system shall compare the target role requirements with the candidate's available profile information.

### FR-AI-022

The system shall identify potential skill gaps.

### FR-AI-023

The system may provide learning recommendations based on identified gaps.

---

# 16. AI Career Assistant

### FR-AI-030

The system shall provide an AI-based career assistant.

### FR-AI-031

The assistant shall answer supported career-related questions.

### FR-AI-032

The assistant may use candidate-specific information when authorized.

### FR-AI-033

The system shall protect private candidate information from unauthorized AI processing.

### FR-AI-034

The system should provide relevant context to the LLM rather than blindly sending the entire application database.

---

# 17. RAG Requirements

### FR-RAG-001

The system shall support retrieval-augmented generation for selected AI use cases.

### FR-RAG-002

Relevant documents shall be processed into suitable chunks.

### FR-RAG-003

The system shall generate embeddings for supported knowledge sources.

### FR-RAG-004

Embeddings shall be stored in a suitable vector storage system.

### FR-RAG-005

The system shall retrieve relevant information for supported user queries.

### FR-RAG-006

Retrieved information shall be supplied to the LLM as contextual information.

### FR-RAG-007

The system should provide mechanisms to reduce unsupported AI responses.

---

# 18. AI Interview Practice

### FR-AI-040

Candidates shall be able to initiate an AI interview session for a selected role.

### FR-AI-041

The system shall generate interview questions appropriate to the selected role.

### FR-AI-042

Candidates shall be able to submit answers.

### FR-AI-043

The system may provide AI-generated feedback about submitted answers.

### FR-AI-044

The system shall clearly identify AI-generated feedback as assistance rather than an official hiring evaluation.

---

# 19. Search Requirements

### FR-SEARCH-001

The system shall support job search.

### FR-SEARCH-002

The system shall support filtering.

### FR-SEARCH-003

The system shall support pagination for large result sets.

### FR-SEARCH-004

The system should support efficient searching over large datasets.

### FR-SEARCH-005

The system may introduce a dedicated search engine if database search becomes insufficient at scale.

---

# 20. Non-Functional Requirements

## 20.1 Performance

### NFR-PERF-001

The system should provide acceptable response times for normal API operations.

### NFR-PERF-002

Long-running operations such as resume processing should not unnecessarily block user-facing requests.

### NFR-PERF-003

The system should support pagination for potentially large datasets.

---

## 20.2 Scalability

### NFR-SCALE-001

Individual services should be independently scalable where required.

### NFR-SCALE-002

The architecture should support horizontal scaling.

### NFR-SCALE-003

Stateless application services should be preferred where practical.

---

## 20.3 Availability

### NFR-AVAIL-001

Failure of a non-critical service should not unnecessarily make the entire platform unavailable.

### NFR-AVAIL-002

The system should implement appropriate timeout and failure-handling mechanisms for dependent services.

---

## 20.4 Security

### NFR-SEC-001

Sensitive credentials shall not be stored in plain text.

### NFR-SEC-002

Protected APIs shall require appropriate authentication.

### NFR-SEC-003

Authorization shall be enforced on the backend.

### NFR-SEC-004

Users shall not be able to access another user's private information without authorization.

### NFR-SEC-005

Uploaded files shall be validated and handled securely.

### NFR-SEC-006

Secrets and API keys shall not be committed to the Git repository.

### NFR-SEC-007

The system should support rate limiting for sensitive or abuse-prone APIs.

---

# 21. Reliability

### NFR-REL-001

The system shall prevent duplicate critical operations where required.

### NFR-REL-002

The system should handle transient service failures gracefully.

### NFR-REL-003

Asynchronous operations should support appropriate retry mechanisms.

### NFR-REL-004

Message processing should consider idempotency.

---

# 22. Data Integrity

### NFR-DATA-001

The system shall enforce appropriate database constraints.

### NFR-DATA-002

Relationships between entities shall maintain referential integrity where applicable.

### NFR-DATA-003

Critical database operations shall use appropriate transaction boundaries.

### NFR-DATA-004

The system shall maintain application status consistency.

---

# 23. Observability

### NFR-OBS-001

Services shall produce structured logs.

### NFR-OBS-002

Important application metrics should be collected.

### NFR-OBS-003

The system should support health checks.

### NFR-OBS-004

The system should support distributed tracing for important cross-service requests.

---

# 24. Deployment Requirements

### NFR-DEP-001

Services shall be containerized using Docker.

### NFR-DEP-002

The project shall use automated CI/CD workflows.

### NFR-DEP-003

The application shall be deployable to a cloud environment.

### NFR-DEP-004

Environment-specific configuration shall be separated from source code.

### NFR-DEP-005

Secrets shall be managed using appropriate secret-management mechanisms.

---

# 25. Technology Learning Requirements

The project is also intended as an engineering-learning project.

The implementation should provide practical exposure to:

### Backend

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- Spring Security

### Database

- PostgreSQL
- SQL
- Indexing
- Transactions
- Query optimization
- Database design

### Distributed Systems

- Microservices
- REST
- Event-driven architecture
- Kafka
- Redis
- Asynchronous processing
- Retry mechanisms
- Idempotency
- Circuit breakers

### AI

- LLM APIs
- Prompt engineering
- Structured outputs
- Embeddings
- Vector databases
- RAG
- AI evaluation
- Tool/function calling where appropriate

### DevOps / Cloud

- Git
- GitHub
- Docker
- CI/CD
- AWS
- Infrastructure configuration
- Monitoring
- Logging
- Observability

---

# 26. Business Rules

### BR-001

A candidate must be authenticated before applying for a job.

### BR-002

A recruiter must be authorized to manage a job posting.

### BR-003

A candidate cannot submit a duplicate application for the same job.

### BR-004

Candidates cannot apply for jobs that are closed or expired.

### BR-005

Only valid job postings can be published.

### BR-006

Recruiters cannot modify applications belonging to another recruiter's job.

### BR-007

Application status changes shall follow defined valid transitions.

### BR-008

AI-generated information shall not automatically make final hiring decisions.

### BR-009

Private candidate information shall only be accessible to authorized users.

### BR-010

AI services shall only receive the minimum required information for a particular operation where practical.

---

# 27. Application Status Lifecycle

The initial application lifecycle is:

APPLIED
   │
   ▼
UNDER_REVIEW
   │
   ├──────────────► REJECTED
   │
   ▼
SHORTLISTED
   │
   ▼
INTERVIEW
   │
   ├──────────────► REJECTED
   │
   ▼
OFFERED
   │
   ├──────────────► ACCEPTED
   │
   └──────────────► DECLINED
```

The exact state-transition rules will be finalized during system design.

---

# 28. Initial Success Criteria

The project will be considered functionally complete when:

1. Candidates can register and create profiles.
2. Recruiters can create companies and job postings.
3. Candidates can search and apply for jobs.
4. Recruiters can manage applications.
5. Application status changes work correctly.
6. Notifications work for important events.
7. Resume processing works for supported formats.
8. AI-assisted resume analysis works.
9. AI-assisted job matching works.
10. Skill-gap analysis works.
11. RAG-based career assistance works.
12. The application is covered by appropriate automated tests.
13. Services can run using Docker.
14. CI/CD is configured.
15. The application is deployed to the cloud.
16. Logs, health checks, and relevant metrics are available.
17. The architecture and important technical decisions are documented.

---

# 29. Future Scope

Potential future capabilities include:

- Advanced recommendation systems
- Interview scheduling
- Calendar integration
- Email integration
- External job-board integrations
- Advanced recruiter analytics
- Candidate ranking assistance with explainability
- Fraud/spam detection
- Multilingual AI assistant
- Voice-based interview practice
- Real-time interview assistance
- Advanced search infrastructure
- Kubernetes-based deployment
- Advanced AI agents and tool use

---

# 30. Development Principle

CareerForge will be developed incrementally.

The project will follow:

**Requirements → Database Design → System Design → Implementation → Testing → Deployment → Monitoring**

Technology decisions shall be justified based on actual system requirements rather than introducing technologies solely for demonstration.

AI features shall be designed as assistive capabilities and shall not be treated as autonomous decision-makers for employment outcomes.