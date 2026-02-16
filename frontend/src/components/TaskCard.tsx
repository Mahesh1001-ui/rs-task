import { Trash2, ChevronRight, ChevronLeft } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import type { Task, TaskStatus } from '@/types'
import { cn } from '@/lib/utils'

interface TaskCardProps {
  task: Task
  onMove: (taskId: string, newStatus: TaskStatus) => void
  onDelete: (taskId: string) => void
  isHighlighted?: boolean
}

const priorityColors = {
  LOW: 'bg-green-500',
  MEDIUM: 'bg-yellow-500',
  HIGH: 'bg-red-500',
}

const statusOrder: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE']

export function TaskCard({ task, onMove, onDelete, isHighlighted }: TaskCardProps) {
  const currentStatusIndex = statusOrder.indexOf(task.status)
  const canMoveRight = currentStatusIndex < statusOrder.length - 1
  const canMoveLeft = currentStatusIndex > 0

  const handleMoveRight = () => {
    if (canMoveRight) {
      onMove(task.id, statusOrder[currentStatusIndex + 1])
    }
  }

  const handleMoveLeft = () => {
    if (canMoveLeft) {
      onMove(task.id, statusOrder[currentStatusIndex - 1])
    }
  }

  return (
    <Card
      className={cn(
        'mb-3 transition-all',
        isHighlighted && 'animate-flash ring-2 ring-primary'
      )}
    >
      <CardHeader className="p-4 pb-2">
        <div className="flex items-start justify-between gap-2">
          <CardTitle className="text-base font-medium">{task.title}</CardTitle>
          <Badge
            className={cn('text-xs text-white', priorityColors[task.priority])}
            role="status"
            aria-label={`Priority: ${task.priority}`}
          >
            {task.priority}
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="p-4 pt-0">
        {task.description && (
          <p className="text-sm text-muted-foreground mb-3 line-clamp-2">
            {task.description}
          </p>
        )}

        <div className="flex items-center justify-between">
          <div className="flex gap-1">
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8"
              onClick={handleMoveLeft}
              disabled={!canMoveLeft}
              title="Move to previous column"
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8"
              onClick={handleMoveRight}
              disabled={!canMoveRight}
              title="Move to next column"
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>

          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8 text-destructive hover:text-destructive"
            onClick={() => onDelete(task.id)}
            title="Delete task"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>

        <p className="text-xs text-muted-foreground mt-2">
          {new Date(task.updatedAt).toLocaleString()}
        </p>
      </CardContent>
    </Card>
  )
}
