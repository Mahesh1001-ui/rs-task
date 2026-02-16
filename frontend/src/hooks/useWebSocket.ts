import { useEffect, useRef, useState, useCallback } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import type { TaskEvent, BoardEvent } from '@/types'

type WebSocketStatus = 'connecting' | 'connected' | 'disconnected' | 'error'

interface UseWebSocketOptions {
  boardId: string
  onTaskEvent?: (event: TaskEvent) => void
  onBoardEvent?: (event: BoardEvent) => void
}

export function useWebSocket({ boardId, onTaskEvent, onBoardEvent }: UseWebSocketOptions) {
  const [status, setStatus] = useState<WebSocketStatus>('disconnected')
  const wsRef = useRef<WebSocket | null>(null)
  const reconnectTimeoutRef = useRef<number | null>(null)
  const reconnectAttemptsRef = useRef(0)
  const queryClient = useQueryClient()

  const connect = useCallback(() => {
    if (!boardId) return

    setStatus('connecting')

    const wsUrl = `ws://${window.location.hostname}:8080/ws/taskboard?boardId=${boardId}`
    const ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      setStatus('connected')
      reconnectAttemptsRef.current = 0
      console.log('WebSocket connected to board:', boardId)
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)

        // Handle task events
        if (data.type?.startsWith('TASK_')) {
          const taskEvent = data as TaskEvent
          onTaskEvent?.(taskEvent)

          // Invalidate task queries to refresh data
          queryClient.invalidateQueries({ queryKey: ['tasks', boardId] })
        }

        // Handle board events
        if (data.type?.startsWith('BOARD_')) {
          const boardEvent = data as BoardEvent
          onBoardEvent?.(boardEvent)

          // Invalidate board queries to refresh data
          queryClient.invalidateQueries({ queryKey: ['boards'] })
        }
      } catch (error) {
        console.error('Failed to parse WebSocket message:', error)
      }
    }

    ws.onerror = (error) => {
      console.error('WebSocket error:', error)
      setStatus('error')
    }

    ws.onclose = () => {
      setStatus('disconnected')
      wsRef.current = null

      // Attempt to reconnect with exponential backoff
      const maxAttempts = 5
      if (reconnectAttemptsRef.current < maxAttempts) {
        const delay = Math.min(1000 * Math.pow(2, reconnectAttemptsRef.current), 30000)
        reconnectAttemptsRef.current += 1

        console.log(`WebSocket disconnected. Reconnecting in ${delay}ms...`)
        reconnectTimeoutRef.current = window.setTimeout(() => {
          connect()
        }, delay)
      }
    }

    wsRef.current = ws
  }, [boardId, onTaskEvent, onBoardEvent, queryClient])

  useEffect(() => {
    connect()

    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current)
      }
      if (wsRef.current) {
        wsRef.current.close()
      }
    }
  }, [connect])

  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current)
    }
    reconnectAttemptsRef.current = Infinity // Prevent reconnection
    if (wsRef.current) {
      wsRef.current.close()
    }
  }, [])

  return {
    status,
    disconnect,
    reconnect: connect,
  }
}
