# Realtime Task Board

## 📋 Overview

Welcome to the Real-Time Collaborative Task Board take-home exercise!

This exercise is designed to assess your full-stack development skills, understanding of distributed systems, and adherence to Test-Driven Development (TDD) practices. You are provided with a skeleton project that has basic board CRUD functionality implemented with full test coverage.

**Your task is to extend this application by implementing additional features while following TDD principles.**

---

## 📦 Getting Started

### Step 1: Extract the Project

You received this project as a zip file attachment. Extract it to your preferred working directory:

```bash
# Extract the zip file
unzip realtime-taskboard.zip

# Navigate to the project directory
cd realtime-taskboard
```

> **Important**: The zip file contains a `.git` directory with the project's git history. This is intentional - you should continue using git for version control during your implementation. Your commit history will be reviewed as part of the evaluation.

### Step 2: Verify Git History

```bash
# Check that git history is intact
git log --oneline -5

# You should see the initial commits from the project setup
```

### Step 3: Create Your Working Branch (Optional but Recommended)

```bash
# Create a branch for your implementation
git checkout -b implementation

# This keeps the original commits clean and shows your work clearly
```

## ⏱️ Time Expectation

- **Expected completion time**: 2 hours
- **Use of AI tools (Cursor, Copilot, etc.)**: Encouraged
- **Focus**: Quality over quantity - it's better to complete fewer stories with excellent tests than many stories with poor coverage

## 🎯 What We're Evaluating

1. **TDD Practice**: Write tests BEFORE implementation
2. **Code Quality**: Clean, readable, maintainable code
3. **Understanding of Distributed Systems**: Proper handling of real-time events
4. **Problem Solving**: Debugging skills and logical thinking
5. **Software Design**: Proper separation of concerns, SOLID principles

## 🔧 Pre-Implementation Tasks

### Task 0: Environment Setup & Bug Fixes (Required - 20 minutes)

Before implementing new features, you must:

1. **Set up the development environment** using one of the following methods:

   **Option A: Using Docker (Recommended - Fastest Setup)**
   ```bash
   # Start the entire application with one command
   docker compose up --build

   # Or for development with hot-reloading
   docker compose -f docker-compose.dev.yml up --build
   ```

   **Option B: Manual Setup**
   ```bash
   # Backend (Terminal 1)
   cd backend && ./gradlew bootRun

   # Frontend (Terminal 2)
   cd frontend && npm install && npm run dev
   ```

2. **Run all existing tests** - you'll notice some tests are failing
   ```bash
   # Backend tests
   cd backend && ./gradlew test

   # Frontend tests
   cd frontend && npm test

   # Or with Docker
   docker compose exec backend ./gradlew test
   docker compose exec frontend npm test
   ```

3. **Fix the intentional bugs** in the codebase

#### Known Issues to Fix:

**Bug #1: Board Service - Incorrect Soft Delete** (Backend)
- Location: `BoardService.java`
- Symptom: When deleting a board, it's being hard deleted instead of soft deleted
- The test `shouldSoftDeleteBoard` is failing
- Fix the implementation to properly set the `deleted` flag instead of removing the entity

**Bug #2: WebSocket Message Serialization** (Backend)
- Location: `WebSocketEventHandler.java`
- Symptom: WebSocket messages are not being received by clients
- The integration test `shouldBroadcastTaskCreatedEvent` is failing
- Hint: Check the message conversion/serialization

**Bug #3: Task Status Validation** (Backend)
- Location: `TaskService.java`
- Symptom: Tasks can be moved to invalid statuses
- The test `shouldRejectInvalidStatusTransition` is failing
- Implement proper status transition validation (TODO → IN_PROGRESS → DONE only)

**Bug #4: React Query Cache Invalidation** (Frontend)
- Location: `useBoards.ts` hook
- Symptom: After creating a board, the list doesn't update
- The component test is failing
- Fix the cache invalidation after mutation

> **Important**: All bugs must be fixed with the existing tests passing before proceeding to new features.

---

## 📝 User Stories to Implement

Implement as many of the following user stories as possible within the time limit. Stories are ordered by priority - complete them in order.

---

### Story 1: Task Priority Management (Must Have)

**As a** team member
**I want to** set and update task priorities
**So that** I can identify which tasks need immediate attention

#### Acceptance Criteria:

```gherkin
Feature: Task Priority Management

  Scenario: Create task with priority
    Given I am on a board page
    When I create a new task with title "Urgent Bug Fix" and priority "HIGH"
    Then the task should be created with priority "HIGH"
    And the task card should display a red priority badge

  Scenario: Update task priority
    Given a task exists with priority "LOW"
    When I update the task priority to "HIGH"
    Then the task priority should be changed to "HIGH"
    And all connected clients should see the updated priority

  Scenario: Filter tasks by priority
    Given multiple tasks exist with different priorities
    When I filter by priority "HIGH"
    Then only tasks with priority "HIGH" should be visible

  Scenario: Priority validation
    Given I am creating a task
    When I try to set an invalid priority value
    Then I should receive a validation error
    And the task should not be created
```

#### Technical Requirements:
- Priority enum: `LOW`, `MEDIUM`, `HIGH`
- Backend validation for priority values
- Frontend priority badge component with colors (LOW=green, MEDIUM=yellow, HIGH=red)
- WebSocket broadcast on priority change
- Unit tests for service layer
- Component tests for priority badge

---

### Story 2: Task Movement Between Columns (Must Have)

**As a** team member
**I want to** move tasks between columns (TODO → IN_PROGRESS → DONE)
**So that** I can update the status of my work

#### Acceptance Criteria:

```gherkin
Feature: Task Movement

  Scenario: Move task to next status
    Given a task exists in "TODO" column
    When I move the task to "IN_PROGRESS"
    Then the task should appear in the "IN_PROGRESS" column
    And all connected clients should see the task in the new column
    And a TASK_MOVED event should be broadcast

  Scenario: Prevent invalid status transitions
    Given a task exists in "TODO" column
    When I try to move the task directly to "DONE"
    Then I should receive an error "Invalid status transition"
    And the task should remain in "TODO"

  Scenario: Move completed task back to in progress
    Given a task exists in "DONE" column
    When I move the task back to "IN_PROGRESS"
    Then the task should appear in the "IN_PROGRESS" column

  Scenario: Concurrent move conflict
    Given two users are viewing the same board
    And both try to move the same task simultaneously
    Then only one move should succeed
    And the other user should see the updated state
```

#### Technical Requirements:
- `PATCH /api/tasks/{id}/move` endpoint
- Status transition validation logic
- Optimistic locking for concurrent updates
- WebSocket event: `TASK_MOVED` with previous and new status
- Frontend drag-and-drop (optional) or move buttons

---

### Story 3: Real-Time Task Updates via WebSocket (Must Have)

**As a** team member
**I want to** see task changes in real-time without refreshing
**So that** I always have the latest information

#### Acceptance Criteria:

```gherkin
Feature: Real-Time Updates

  Scenario: Receive task creation event
    Given I am connected to a board's WebSocket
    When another user creates a task on the same board
    Then I should see the new task appear automatically
    And the task should have a brief highlight animation

  Scenario: Receive task update event
    Given I am connected to a board's WebSocket
    When another user updates a task
    Then I should see the changes immediately

  Scenario: Handle WebSocket disconnection
    Given I am connected to a board's WebSocket
    When my connection is lost
    Then I should see a "Reconnecting..." indicator
    And the connection should automatically retry
    And once reconnected, I should receive any missed updates

  Scenario: Subscribe to specific board
    Given multiple boards exist
    When I open board "A"
    Then I should only receive events for board "A"
    And I should not receive events for board "B"
```

#### Technical Requirements:
- WebSocket endpoint: `/ws/taskboard`
- Board-specific subscription via query param or message
- Connection state management in frontend
- Automatic reconnection with exponential backoff
- Event types: `TASK_CREATED`, `TASK_UPDATED`, `TASK_DELETED`, `TASK_MOVED`
- Integration tests for WebSocket handlers

---

### Story 4: Task Assignment (Should Have)

**As a** team lead
**I want to** assign tasks to team members
**So that** everyone knows their responsibilities

#### Acceptance Criteria:

```gherkin
Feature: Task Assignment

  Scenario: Assign task to user
    Given a task exists without an assignee
    And users "Alice" and "Bob" exist in the system
    When I assign the task to "Alice"
    Then the task should show "Alice" as the assignee
    And all connected clients should see the assignment

  Scenario: Unassign task
    Given a task is assigned to "Alice"
    When I remove the assignment
    Then the task should show as unassigned

  Scenario: Reassign task
    Given a task is assigned to "Alice"
    When I reassign the task to "Bob"
    Then the task should show "Bob" as the assignee
    And "Alice" should receive a notification (if notifications implemented)

  Scenario: Filter tasks by assignee
    Given multiple tasks are assigned to different users
    When I filter by assignee "Alice"
    Then only tasks assigned to "Alice" should be visible
```

#### Technical Requirements:
- `PUT /api/tasks/{id}/assign` endpoint
- User entity and repository (simplified, no auth required)
- Assignee displayed with avatar or initials on task card
- WebSocket event: `TASK_ASSIGNED`

---

### Story 5: Due Dates and Overdue Indicator (Should Have)

**As a** team member
**I want to** set due dates on tasks
**So that** I can track deadlines and identify overdue items

#### Acceptance Criteria:

```gherkin
Feature: Due Dates

  Scenario: Set due date on task
    Given a task exists without a due date
    When I set the due date to tomorrow
    Then the task should display the due date

  Scenario: Display overdue indicator
    Given a task has a due date in the past
    And the task is not in "DONE" status
    When I view the task card
    Then the task should display an "Overdue" badge in red

  Scenario: Remove overdue indicator when completed
    Given a task is overdue
    When I move the task to "DONE"
    Then the "Overdue" badge should be removed

  Scenario: Update due date
    Given a task has a due date
    When I update the due date
    Then the new due date should be displayed
    And all connected clients should see the change
```

#### Technical Requirements:
- Due date field on Task entity
- `PATCH /api/tasks/{id}` endpoint accepts due date
- Frontend date picker component
- Overdue calculation logic (client-side)
- Visual indicator for overdue tasks

---

### Story 6: Server-Sent Events (SSE) Alternative (Should Have)

**As a** developer
**I want to** receive real-time updates via SSE
**So that** I have an alternative to WebSockets for simpler clients

#### Acceptance Criteria:

```gherkin
Feature: SSE Support

  Scenario: Connect to SSE stream
    Given I make a GET request to /api/boards/{id}/events
    With header Accept: text/event-stream
    Then I should receive a persistent connection
    And the content-type should be "text/event-stream"

  Scenario: Receive events via SSE
    Given I am connected to the SSE stream for board "A"
    When a task is created on board "A"
    Then I should receive an event with type "TASK_CREATED"
    And the data should contain the task details

  Scenario: Handle client disconnect
    Given a client is connected to the SSE stream
    When the client disconnects
    Then the server should clean up the connection
    And no memory leak should occur
```

#### Technical Requirements:
- `GET /api/boards/{id}/events` endpoint using `SseEmitter`
- Event format following SSE specification
- Proper connection cleanup
- Same events as WebSocket (can share event publishing logic)
- Integration tests for SSE endpoint

---

### Story 7: Task Search (Should Have)

**As a** team member
**I want to** search for tasks by title or description
**So that** I can quickly find specific tasks

#### Acceptance Criteria:

```gherkin
Feature: Task Search

  Scenario: Search by title
    Given tasks exist with titles "Login Bug", "Payment Feature", "Login UI Update"
    When I search for "Login"
    Then I should see "Login Bug" and "Login UI Update"
    And I should not see "Payment Feature"

  Scenario: Search by description
    Given a task exists with description "Fix the authentication flow"
    When I search for "authentication"
    Then I should see the task in results

  Scenario: Case insensitive search
    Given a task exists with title "LOGIN BUG"
    When I search for "login"
    Then I should see the task in results

  Scenario: Empty search
    Given tasks exist
    When I search with empty query
    Then I should see all tasks
```

#### Technical Requirements:
- `GET /api/boards/{id}/tasks?search={query}` query parameter
- Case-insensitive search in title and description
- Frontend search input with debounced API calls
- Highlight matched text (optional)

---

### Story 8: Activity Log / Audit Trail (Could Have)

**As a** team lead
**I want to** see a history of changes on a board
**So that** I can track what happened and when

#### Acceptance Criteria:

```gherkin
Feature: Activity Log

  Scenario: Log task creation
    When a task is created
    Then an activity entry should be recorded
    With action "TASK_CREATED"
    And the task details

  Scenario: Log task movement
    When a task is moved from "TODO" to "IN_PROGRESS"
    Then an activity entry should be recorded
    With action "TASK_MOVED"
    And previous status "TODO"
    And new status "IN_PROGRESS"

  Scenario: View activity log
    Given multiple activities have occurred on a board
    When I request the activity log
    Then I should see activities in reverse chronological order
    With timestamps and action details

  Scenario: Paginate activity log
    Given more than 50 activities exist
    When I request the activity log
    Then I should receive paginated results
```

#### Technical Requirements:
- ActivityLog entity
- Automatic logging via event listeners
- `GET /api/boards/{id}/activities` endpoint with pagination
- Activity display component in frontend

---

### Story 9: Board Statistics Dashboard (Could Have)

**As a** team lead
**I want to** see statistics about the board
**So that** I can understand team progress

#### Acceptance Criteria:

```gherkin
Feature: Board Statistics

  Scenario: View task counts by status
    Given a board has 5 TODO, 3 IN_PROGRESS, and 10 DONE tasks
    When I view the statistics
    Then I should see counts for each status

  Scenario: View completion rate
    Given a board has tasks
    When I view the statistics
    Then I should see the percentage of completed tasks

  Scenario: Statistics update in real-time
    Given I am viewing the statistics
    When a task is moved to DONE
    Then the statistics should update automatically
```

#### Technical Requirements:
- `GET /api/boards/{id}/stats` endpoint
- Statistics DTO with counts and percentages
- Frontend statistics component with charts (optional)
- Real-time updates via WebSocket

---

## 📤 Submission Guidelines

### What to Submit

1. **Complete source code** with your implementation
2. **All tests passing** (both existing and new)
3. **Git history** showing your TDD approach (RED-GREEN-REFACTOR commits)
4. **Brief documentation** of your implementation decisions (optional)

### Submission Checklist

Before creating your submission zip file, verify the following:

- [ ] All pre-existing tests pass
- [ ] All bugs fixed
- [ ] New features have tests written BEFORE implementation
- [ ] Code compiles and runs without errors
- [ ] Application runs successfully with `docker compose up --build`
- [ ] README updated if needed
- [ ] Git history shows TDD approach (test commits before implementation)

### Creating the Submission Zip File

**Important**: Include the `.git` directory in your submission so we can review your commit history. This is crucial for evaluating your TDD approach.

**File Naming Convention:**
```
submission-<your-name>-<date>.zip
```
- **Your name**: Use kebab-case (lowercase, hyphens instead of spaces)
- **Date**: Use `dd-mm-yyyy` format

**Examples:**
- `submission-john-doe-15-02-2026.zip`
- `submission-jane-smith-20-02-2026.zip`

**On macOS/Linux:**
```bash
# Navigate to the parent directory of the project
cd ..

# Create the zip file (replace YOUR-NAME and DATE)
# Example: submission-john-doe-15-02-2026.zip
zip -r submission-<your-name>-<date>.zip realtime-taskboard \
  -x "realtime-taskboard/node_modules/*" \
  -x "realtime-taskboard/frontend/node_modules/*" \
  -x "realtime-taskboard/backend/build/*" \
  -x "realtime-taskboard/backend/.gradle/*"
```

**On Windows (PowerShell):**
> **Note:** If you encounter a "PermissionDenied" error due to git processes locking files, stop them first:
> ```powershell
> Get-Process git*
> Stop-Process -Name git -Force
> ```

```powershell
# Ensure you're in the parent directory
# Replace YOUR-NAME and DATE accordingly
Compress-Archive -Path realtime-taskboard -DestinationPath submission-your-name-dd-mm-yyyy.zip
# Note: You may need to manually delete node_modules and build folders before zipping
```

### What to Include / Exclude

| Include ✅ | Exclude ❌ |
|-----------|-----------|
| All source code (backend & frontend) | `node_modules/` |
| `.git` directory (commit history) | `backend/build/` |
| Configuration files | `backend/.gradle/` |
| Updated documentation | IDE-specific files (`.idea/`, `.vscode/`) |

### How to Submit

1. Create your submission zip file following the naming convention above
2. Verify the zip file size is reasonable (should be under 10MB)
3. **Reply to the recruiter's email** with:
   - Your `submission-<your-name>-<date>.zip` file attached
   - A brief note about your implementation approach (optional but appreciated)

### Evaluation Timing

- We will review your submission within 3-5 business days
- You may be asked for a follow-up call to discuss your implementation

---

## 💡 Tips for Success

### TDD Best Practices
```
1. Write a failing test
2. Write minimal code to pass
3. Refactor
4. Repeat
```

### Git Commit Strategy
- Commit tests before implementation
- Use meaningful commit messages
- Example commit flow:
  ```
  test: add failing test for task priority update
  feat: implement task priority update in service
  test: add component test for priority badge
  feat: implement priority badge component
  refactor: extract priority color logic
  ```

### Time Management
- 10 min: Setup with Docker (`docker compose up --build`)
- 20 min: Bug fixes
- 30 min: Story 1 (Priority)
- 30 min: Story 2 (Movement)
- 20 min: Story 3 (WebSocket)
- 10 min: Final testing and cleanup

### When Using AI Tools
- Review generated code carefully
- Ensure tests actually test behavior
- Don't blindly accept suggestions
- Make sure you understand the code

---

## ❓ Questions?

If you have questions about the requirements, please reach out to the recruiter. We want to ensure you have a clear understanding of expectations.

**Good luck! We're excited to see your solution.** 🚀
