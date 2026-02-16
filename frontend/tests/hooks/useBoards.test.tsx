import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useBoards, useCreateBoard, BOARDS_QUERY_KEY } from '@/hooks/useBoards'

// Mock the API
vi.mock('@/services/api', () => ({
  boardApi: {
    getAll: vi.fn(),
    create: vi.fn(),
  },
}))

import { boardApi } from '@/services/api'

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  )
}

describe('useBoards', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should fetch boards', async () => {
    const mockBoards = [{ id: '1', name: 'Test Board' }]
    ;(boardApi.getAll as ReturnType<typeof vi.fn>).mockResolvedValue(mockBoards)

    const { result } = renderHook(() => useBoards(), { wrapper: createWrapper() })

    await waitFor(() => {
      expect(result.current.data).toEqual(mockBoards)
    })

    expect(boardApi.getAll).toHaveBeenCalled()
  })
})

describe('useCreateBoard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should create board', async () => {
    const newBoard = { id: '2', name: 'New Board' }
    ;(boardApi.create as ReturnType<typeof vi.fn>).mockResolvedValue(newBoard)

    const { result } = renderHook(() => useCreateBoard(), { wrapper: createWrapper() })

    await result.current.mutateAsync({ name: 'New Board' })

    expect(boardApi.create).toHaveBeenCalledWith({ name: 'New Board' })
  })

  it('should invalidate boards query after creation', async () => {
    // BUG #4 Test: This test verifies cache invalidation
    // Currently fails because invalidateQueries is not called in onSuccess
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries')
    const newBoard = { id: '2', name: 'New Board' }
    ;(boardApi.create as ReturnType<typeof vi.fn>).mockResolvedValue(newBoard)

    const wrapper = ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    )

    const { result } = renderHook(() => useCreateBoard(), { wrapper })

    await result.current.mutateAsync({ name: 'New Board' })

    // This assertion will fail until BUG #4 is fixed
    // The onSuccess callback should call queryClient.invalidateQueries({ queryKey: BOARDS_QUERY_KEY })
    await waitFor(() => {
      expect(invalidateSpy).toHaveBeenCalledWith({ queryKey: BOARDS_QUERY_KEY })
    })
  })
})
