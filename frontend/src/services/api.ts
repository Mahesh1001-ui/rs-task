import type {
  Board,
  Task,
  CreateBoardRequest,
  CreateTaskRequest,
  MoveTaskRequest,
} from '@/types'

const API_BASE_URL = '/api'

async function handleResponse<T>(response: Response): Promise<T> {


  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'An error occurred' }))
    throw new Error(error.message || `HTTP error! status: ${response.status}`)
  }

  // Handle 204 No Content
  if (response.status === 204) {
    return undefined as T
  }

  return response.json()
}

// Board API
export const boardApi = {
  getAll: async (): Promise<Board[]> => {
    const response = await fetch(`${API_BASE_URL}/boards`)
    return handleResponse<Board[]>(response)
  },

  getById: async (boardId: string): Promise<Board> => {
    const response = await fetch(`${API_BASE_URL}/boards/${boardId}`)
    return handleResponse<Board>(response)
  },

  create: async (data: CreateBoardRequest): Promise<Board> => {
    const response = await fetch(`${API_BASE_URL}/boards`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return handleResponse<Board>(response)
  },

  update: async (boardId: string, data: CreateBoardRequest): Promise<Board> => {
    const response = await fetch(`${API_BASE_URL}/boards/${boardId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return handleResponse<Board>(response)
  },

  delete: async (boardId: string): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/boards/${boardId}`, {
      method: 'DELETE',
    })
    return handleResponse<void>(response)
  },
}

// Task API
export const taskApi = {
  getByBoardId: async (boardId: string): Promise<Task[]> => {
    const response = await fetch(`${API_BASE_URL}/boards/${boardId}/tasks`)
    return handleResponse<Task[]>(response)
  },

  getById: async (taskId: string): Promise<Task> => {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`)
    return handleResponse<Task>(response)
  },

  create: async (boardId: string, data: CreateTaskRequest): Promise<Task> => {
    const response = await fetch(`${API_BASE_URL}/boards/${boardId}/tasks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return handleResponse<Task>(response)
  },

  update: async (taskId: string, data: CreateTaskRequest): Promise<Task> => {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return handleResponse<Task>(response)
  },

  move: async (taskId: string, data: MoveTaskRequest): Promise<Task> => {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}/move`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    })
    return handleResponse<Task>(response)
  },

  delete: async (taskId: string): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/tasks/${taskId}`, {
      method: 'DELETE',
    })
    return handleResponse<void>(response)
  },
}
