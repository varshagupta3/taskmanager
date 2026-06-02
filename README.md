# Team Task Manager

Java full-stack Team Task Manager for project creation, team membership, task assignment, status tracking, dashboards, and role-based access control.

## Tech Stack

- Java 17, Spring Boot 3.5
- Spring Web, Spring Security, JWT
- Spring Data JPA, PostgreSQL
- Bean Validation
- HTML, CSS, vanilla JavaScript frontend served from Spring Boot

## Features

- Signup and login with JWT authentication
- Roles: `ADMIN` and `MEMBER`
- Admins can create projects, add project members, create tasks, and update any task status
- Members can view projects they belong to and update tasks assigned to them
- Dashboard counts for open assigned tasks, completed tasks, overdue tasks, visible projects, and visible tasks
- REST APIs with validation and relational database mappings

## Local Setup

By default, the application is configured to automatically fall back to an in-memory H2 database if no PostgreSQL environment is configured. This means you can run the app locally with **zero setup**.

To run the application:

1. Execute the Maven wrapper command:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
2. Open your browser to `http://localhost:8081`.

If you prefer using a local PostgreSQL database, make sure it is running, create a database named `taskmanager`, and configure the database environment variables:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/taskmanager"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="your-password"
$env:JWT_SECRET="replace-with-a-long-production-secret"
```

## API Overview

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| POST | `/api/auth/signup` | Public | Create a user |
| POST | `/api/auth/login` | Public | Login and receive JWT |
| GET | `/api/auth/me` | Authenticated | Current user |
| GET | `/api/dashboard` | Authenticated | Dashboard metrics |
| GET | `/api/projects` | Authenticated | Visible projects |
| POST | `/api/projects` | Admin | Create project |
| POST | `/api/projects/{id}/members` | Admin | Add member to project |
| GET | `/api/tasks` | Authenticated | Visible tasks |
| POST | `/api/tasks` | Admin | Create task |
| PATCH | `/api/tasks/{id}/status` | Admin or assignee | Update task status |

Use `Authorization: Bearer <token>` for authenticated API requests.

## Railway Deployment

1. Push this repository to GitHub.
2. In Railway, create a new project from the GitHub repository.
3. Add a Railway PostgreSQL database service.
4. In the app service variables, set:

```text
DATABASE_URL=jdbc:postgresql://<host>:<port>/<database>
DATABASE_USERNAME=<railway-db-user>
DATABASE_PASSWORD=<railway-db-password>
JWT_SECRET=<long-random-secret-at-least-32-characters>
DDL_AUTO=update
```

5. Railway will build the Spring Boot app and run the `Procfile` web command.
6. After deploy, open the generated Railway URL and create an Admin account first.

## Submission Checklist

- Live URL: add your Railway app URL here
- GitHub repo: add your GitHub repository URL here
- Demo video: record a 2-5 minute walkthrough showing signup/login, admin project creation, member assignment, task creation, member status update, and dashboard counts

## Demo Flow

1. Create an `ADMIN` account.
2. Create a `MEMBER` account.
3. Login as Admin, create a project, and add the Member to it.
4. Create a task assigned to the Member with a due date.
5. Login as Member and update the task from `TODO` to `IN_PROGRESS` or `DONE`.
6. Show dashboard totals and overdue behavior.
