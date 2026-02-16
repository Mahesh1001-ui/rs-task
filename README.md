# Real-Time Collaborative Task Board

A real-time collaborative task management application built with **Spring Boot** (Backend) and **React** (Frontend). This project demonstrates modern full-stack development practices including WebSockets, Server-Sent Events (SSE), event-driven architecture, and Test-Driven Development (TDD).

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Running Tests](#running-tests)
- [API Documentation](#api-documentation)
- [WebSocket Events](#websocket-events)
- [Development Guidelines](#development-guidelines)
- [Submission Instructions](#submission-instructions)

## Project Overview

This application allows multiple users to collaborate on task boards in real-time. Users can:
- Create and manage task boards
- Add, update, and delete tasks
- See real-time updates when other users make changes
- Receive notifications for task assignments and updates

## Architecture

```
┌─────────────────┐     WebSocket/SSE      ┌─────────────────┐
│                 │◄─────────────────────► │                 │
│  React Frontend │                        │  Spring Boot    │
│  (Vite + TS)    │◄─────REST API────────► │  Backend        │
│                 │                        │                 │
└─────────────────┘                        └────────┬────────┘
                                                    │
                                           ┌────────▼────────┐
                                           │   H2 Database   │
                                           │   (In-Memory)   │
                                           └─────────────────┘
```

## Tech Stack

### Backend
- **Java 17+**
- **Spring Boot 3.2.x**
- **Spring WebSocket** - Real-time bidirectional communication
- **Spring WebFlux** - Server-Sent Events (SSE)
- **Spring Data JPA** - Data persistence
- **H2 Database** - In-memory database for development
- **Gradle (Kotlin DSL)** - Build tool
- **JUnit 5 + Mockito** - Testing framework
- **AssertJ** - Fluent assertions

### Frontend
- **React 18+**
- **TypeScript**
- **Vite** - Build tool
- **shadcn/ui** - UI component library
- **TailwindCSS** - Styling
- **React Query** - Server state management
- **Vitest** - Testing framework
- **Testing Library** - Component testing

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java JDK 17 or higher**
  ```bash
  java -version
  # Should output: openjdk version "17.x.x" or higher
  ```

- **Node.js 18 or higher & npm**
  ```bash
  node -version
  # Should output: v18.x.x or higher
  npm -version
  # Should output: 9.x.x or higher
  ```

- **Git** (for version control during development)
  ```bash
  git --version
  ```

- **Docker & Docker Compose**
  ```bash
  docker --version
  # Should output: Docker version 24.x.x or higher
  docker compose version
  # Should output: Docker Compose version v2.x.x or higher
  ```

## Project Structure

```
realtime-taskboard/
├── README.md                 # This file
├── INSTRUCTIONS.md           # Take-home exercise instructions
├── docker-compose.yml        # Production Docker Compose
├── docker-compose.dev.yml    # Development Docker Compose (hot-reload)
├── backend/                  # Spring Boot application
│   ├── Dockerfile            # Production Dockerfile
│   ├── Dockerfile.dev        # Development Dockerfile
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/taskboard/
│   │   │   │       ├── TaskBoardApplication.java
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── model/
│   │   │   │       ├── dto/
│   │   │   │       └── exception/
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── application-docker.yml
│   │   └── test/
│   │       └── java/
│   │           └── com/taskboard/
│   └── gradlew
└── frontend/                 # React application
    ├── Dockerfile            # Production Dockerfile (nginx)
    ├── Dockerfile.dev        # Development Dockerfile
    ├── nginx.conf            # Nginx configuration
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json
    ├── src/
    │   ├── components/
    │   ├── hooks/
    │   ├── services/
    │   ├── types/
    │   └── App.tsx
    └── tests/
```

## Getting Started

### Step 1: Extract the Project

Extract the zip file you received via email:

```bash
# Extract the zip file
unzip realtime-taskboard.zip

# Navigate to the project directory
cd realtime-taskboard
```

> **Important**: The zip file contains a `.git` directory with the project's git history. This is intentional - you should continue using git for version control during your implementation.

### Step 2: Verify Git History

```bash
# Check that git history is intact
git log --oneline -5

# You should see the initial commits from the project setup
```

---

### Option A: Using Docker (Recommended - Easiest Setup)

If you have Docker and Docker Compose installed, you can start the entire application with a single command:

```bash
# Start both backend and frontend
docker compose up --build

# Or run in detached mode
docker compose up --build -d
```

The application will be available at:
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console

To stop the containers:
```bash
docker compose down
```

#### Development Mode with Hot Reloading

For development with live code reloading:

```bash
docker compose -f docker-compose.dev.yml up --build
```

This mounts your source code as volumes, so changes are reflected immediately.

---

### Option B: Manual Setup (Without Docker)

#### 1. Start the Backend

```bash
cd backend

# On macOS/Linux
./gradlew bootRun

# On Windows
gradlew.bat bootRun
```

The backend will start on **http://localhost:8080**

#### 2. Start the Frontend

Open a new terminal:

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start on **http://localhost:5173**

#### 3. Verify Setup

1. Open **http://localhost:5173** in your browser
2. You should see the Task Board application
3. Create a new board and verify it appears
4. Open the same URL in another browser tab to test real-time sync

## Running Tests

### Backend Tests

```bash
cd backend

# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "com.taskboard.service.BoardServiceTest"

# Run tests in watch mode (continuous testing)
./gradlew test --continuous
```

Coverage report will be available at: `backend/build/reports/jacoco/test/html/index.html`

### Frontend Tests

```bash
cd frontend

# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage

# Run specific test file
npm test -- BoardList.test.tsx
```

## API Documentation

### REST Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/boards` | Get all boards |
| POST | `/api/boards` | Create a new board |
| GET | `/api/boards/{id}` | Get board by ID |
| PUT | `/api/boards/{id}` | Update board |
| DELETE | `/api/boards/{id}` | Delete board |
| GET | `/api/boards/{id}/tasks` | Get all tasks for a board |
| POST | `/api/boards/{id}/tasks` | Create a task |
| PUT | `/api/tasks/{id}` | Update a task |
| DELETE | `/api/tasks/{id}` | Delete a task |

### Example Request

```bash
# Create a new board
curl -X POST http://localhost:8080/api/boards \
  -H "Content-Type: application/json" \
  -d '{"name": "My First Board", "description": "A test board"}'
```

## WebSocket Events

### Connection

```javascript
const socket = new WebSocket('ws://localhost:8080/ws/taskboard');
```

### Event Types

| Event | Direction | Description |
|-------|-----------|-------------|
| `BOARD_CREATED` | Server → Client | New board created |
| `BOARD_UPDATED` | Server → Client | Board details updated |
| `TASK_CREATED` | Server → Client | New task added |
| `TASK_UPDATED` | Server → Client | Task modified |
| `TASK_DELETED` | Server → Client | Task removed |
| `TASK_MOVED` | Server → Client | Task moved to different column |

### Event Payload Example

```json
{
  "type": "TASK_CREATED",
  "payload": {
    "id": "uuid-here",
    "title": "New Task",
    "status": "TODO",
    "boardId": "board-uuid"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Development Guidelines

### TDD Approach

This project follows Test-Driven Development. When adding new features:

1. **Write a failing test first**
2. **Write minimal code to make the test pass**
3. **Refactor while keeping tests green**

### Code Style

- Backend: Follow Google Java Style Guide
- Frontend: ESLint + Prettier configuration provided
- Use meaningful variable and function names
- Keep functions small and focused

### Commit Messages

Follow conventional commits:
```
feat: add task assignment feature
fix: resolve WebSocket reconnection issue
test: add unit tests for BoardService
refactor: extract event publisher logic
```

## Submission Instructions

### Before Submitting

1. Ensure all tests pass:
   ```bash
   cd backend && ./gradlew test
   cd ../frontend && npm test
   ```

2. Verify the application runs correctly:
   ```bash
   docker compose up --build
   # Or manual setup as described above
   ```

3. Review your git history shows TDD approach:
   ```bash
   git log --oneline
   ```

### Creating the Submission Zip

**Important**: Include the `.git` directory in your submission so we can review your commit history.

**File Naming Convention:**
```
submission-<your-name>-<date>.zip
```
- **Your name**: Use kebab-case (lowercase, hyphens instead of spaces)
- **Date**: Use `dd-mm-yyyy` format
- **Example**: `submission-john-doe-15-02-2026.zip`

```bash
# Navigate to the parent directory of the project
cd ..

# Create the zip file including git history (replace YOUR-NAME and DATE)
zip -r submission-your-name-dd-mm-yyyy.zip realtime-taskboard -x "realtime-taskboard/node_modules/*" -x "realtime-taskboard/frontend/node_modules/*" -x "realtime-taskboard/backend/build/*" -x "realtime-taskboard/backend/.gradle/*"
```

Or on Windows (PowerShell):
> **Note:** If you encounter a "PermissionDenied" error due to git processes locking files, stop them first:
> ```powershell
> Get-Process git*
> Stop-Process -Name git -Force
> ```

```powershell
# Replace your-name and dd-mm-yyyy accordingly
Compress-Archive -Path realtime-taskboard -DestinationPath submission-your-name-dd-mm-yyyy.zip
```

### What to Include

- ✅ All source code (backend and frontend)
- ✅ `.git` directory (for commit history review)
- ✅ Updated documentation if needed
- ❌ `node_modules/` directory
- ❌ `backend/build/` directory
- ❌ `backend/.gradle/` directory

### Send Your Submission

Reply to the recruiter's email with:
1. Your `submission-<your-name>-<date>.zip` file attached (e.g., `submission-john-doe-15-02-2026.zip`)
2. A brief note about your implementation approach (optional but appreciated)

## Troubleshooting

### Docker Issues

#### Containers won't start
```bash
# Check container logs
docker compose logs backend
docker compose logs frontend

# Rebuild from scratch
docker compose down -v
docker compose up --build --force-recreate
```

#### Port already in use
```bash
# Find and kill process using port 8080
lsof -i :8080
kill -9 <PID>

# Or change the port in docker-compose.yml
```

#### Clear Docker cache
```bash
docker system prune -a
docker compose build --no-cache
```

### Manual Setup Issues

#### Backend won't start
- Verify Java 17+ is installed: `java -version`
- Check if port 8080 is available: `lsof -i :8080`
- Clear Gradle cache: `./gradlew clean`

#### Frontend won't start
- Verify Node 18+ is installed: `node -version`
- Delete node_modules and reinstall: `rm -rf node_modules && npm install`
- Check if port 5173 is available

#### WebSocket connection fails
- Ensure backend is running on port 8080
- Check browser console for CORS errors
- Verify WebSocket endpoint URL

## License

This project is created for educational and assessment purposes.

---

**Good luck with your implementation!** 🚀
