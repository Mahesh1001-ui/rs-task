export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE'

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Board {
  id: string
  name: string
  description?: string
  createdAt: string
  updatedAt: string
  taskCount: number
}

export interface Task {
  id: string
  boardId: string
  title: string
  description?: string
  status: TaskStatus
  priority: TaskPriority
  assigneeId?: string
  dueDate?: string
  createdAt: string
  updatedAt: string
}

export interface CreateBoardRequest {
  name: string
  description?: string
}

export interface CreateTaskRequest {
  title: string
  description?: string
  priority?: TaskPriority
  assigneeId?: string
  dueDate?: string
}

export interface MoveTaskRequest {
  status: TaskStatus
}

export type TaskEventType =
  | 'TASK_CREATED'
  | 'TASK_UPDATED'
  | 'TASK_DELETED'
  | 'TASK_MOVED'
  | 'TASK_ASSIGNED'

export type BoardEventType =
  | 'BOARD_CREATED'
  | 'BOARD_UPDATED'
  | 'BOARD_DELETED'

export interface TaskEvent {
  type: TaskEventType
  payload: Task
  boardId: string
  previousStatus?: TaskStatus
  newStatus?: TaskStatus
  timestamp: string
}

export interface BoardEvent {
  type: BoardEventType
  payload?: Board
  boardId: string
  timestamp: string
}
