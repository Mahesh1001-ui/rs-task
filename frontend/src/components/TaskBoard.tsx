import { useState, useCallback } from 'react'
import { ArrowLeft, Plus, Wifi, WifiOff } from 'lucide-react'
import { useTasks, useCreateTask, useMoveTask, useDeleteTask } from '@/hooks/useTasks'
import { useWebSocket } from '@/hooks/useWebSocket'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { TaskColumn } from './TaskColumn'
import type { Board, TaskStatus, TaskPriority, TaskEvent } from '@/types'

interface TaskBoardProps {
  board: Board
  onBack: () => void
}

export function TaskBoard({ board, onBack }: TaskBoardProps) {
  const { data: tasks, isLoading, error } = useTasks(board.id)
  const createTask = useCreateTask(board.id)
  const moveTask = useMoveTask(board.id)
  const deleteTask = useDeleteTask(board.id)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [newTaskTitle, setNewTaskTitle] = useState('')
  const [newTaskDescription, setNewTaskDescription] = useState('')
  const [newTaskPriority, setNewTaskPriority] = useState<TaskPriority>('MEDIUM')
  const [highlightedTaskId, setHighlightedTaskId] = useState<string | null>(null)

  const handleTaskEvent = useCallback((event: TaskEvent) => {
    // Highlight the task that was just modified
    if (event.payload?.id) {
      setHighlightedTaskId(event.payload.id)
      setTimeout(() => setHighlightedTaskId(null), 2000)
    }
  }, [])

  const { status: wsStatus } = useWebSocket({
    boardId: board.id,
    onTaskEvent: handleTaskEvent,
  })

  const handleCreateTask = async () => {
    if (!newTaskTitle.trim()) return

    try {
      await createTask.mutateAsync({
        title: newTaskTitle.trim(),
        description: newTaskDescription.trim() || undefined,
        priority: newTaskPriority,
      })
      setNewTaskTitle('')
      setNewTaskDescription('')
      setNewTaskPriority('MEDIUM')
      setIsDialogOpen(false)
    } catch (error) {
      console.error('Failed to create task:', error)
    }
  }

  const handleMoveTask = async (taskId: string, newStatus: TaskStatus) => {
    try {
      await moveTask.mutateAsync({ taskId, data: { status: newStatus } })
    } catch (error) {
      console.error('Failed to move task:', error)
      alert('Failed to move task. The transition might not be allowed.')
    }
  }

  const handleDeleteTask = async (taskId: string) => {
    if (confirm('Are you sure you want to delete this task?')) {
      try {
        await deleteTask.mutateAsync(taskId)
      } catch (error) {
        console.error('Failed to delete task:', error)
      }
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center text-destructive p-4">
        Failed to load tasks. Please try again.
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={onBack}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <h1 className="text-3xl font-bold">{board.name}</h1>
          <div className="flex items-center gap-2 text-sm">
            {wsStatus === 'connected' ? (
              <span className="flex items-center gap-1 text-green-600">
                <Wifi className="h-4 w-4" />
                Live
              </span>
            ) : wsStatus === 'connecting' ? (
              <span className="flex items-center gap-1 text-yellow-600">
                <Wifi className="h-4 w-4 animate-pulse" />
                Connecting...
              </span>
            ) : (
              <span className="flex items-center gap-1 text-red-600">
                <WifiOff className="h-4 w-4" />
                Disconnected
              </span>
            )}
          </div>
        </div>

        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              New Task
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Task</DialogTitle>
              <DialogDescription>
                Add a new task to your board.
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="title">Title</Label>
                <Input
                  id="title"
                  value={newTaskTitle}
                  onChange={(e) => setNewTaskTitle(e.target.value)}
                  placeholder="Enter task title"
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  value={newTaskDescription}
                  onChange={(e) => setNewTaskDescription(e.target.value)}
                  placeholder="Enter task description (optional)"
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="priority">Priority</Label>
                <select
                  id="priority"
                  value={newTaskPriority}
                  onChange={(e) => setNewTaskPriority(e.target.value as TaskPriority)}
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                </select>
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleCreateTask} disabled={!newTaskTitle.trim() || createTask.isPending}>
                {createTask.isPending ? 'Creating...' : 'Create Task'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {board.description && (
        <p className="text-muted-foreground mb-6">{board.description}</p>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <TaskColumn
          title="To Do"
          status="TODO"
          tasks={tasks || []}
          onMoveTask={handleMoveTask}
          onDeleteTask={handleDeleteTask}
          highlightedTaskId={highlightedTaskId}
        />
        <TaskColumn
          title="In Progress"
          status="IN_PROGRESS"
          tasks={tasks || []}
          onMoveTask={handleMoveTask}
          onDeleteTask={handleDeleteTask}
          highlightedTaskId={highlightedTaskId}
        />
        <TaskColumn
          title="Done"
          status="DONE"
          tasks={tasks || []}
          onMoveTask={handleMoveTask}
          onDeleteTask={handleDeleteTask}
          highlightedTaskId={highlightedTaskId}
        />
      </div>
    </div>
  )
}
