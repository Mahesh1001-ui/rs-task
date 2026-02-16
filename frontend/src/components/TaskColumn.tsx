import { TaskCard } from './TaskCard'
import type { Task, TaskStatus } from '@/types'

interface TaskColumnProps {
  title: string
  status: TaskStatus
  tasks: Task[]
  onMoveTask: (taskId: string, newStatus: TaskStatus) => void
  onDeleteTask: (taskId: string) => void
  highlightedTaskId?: string | null
}

const columnColors = {
  TODO: 'bg-slate-100 dark:bg-slate-800',
  IN_PROGRESS: 'bg-blue-50 dark:bg-blue-950',
  DONE: 'bg-green-50 dark:bg-green-950',
}

export function TaskColumn({
  title,
  status,
  tasks,
  onMoveTask,
  onDeleteTask,
  highlightedTaskId,
}: TaskColumnProps) {
  const filteredTasks = tasks.filter((task) => task.status === status)

  return (
    <div className={`flex flex-col rounded-lg p-4 ${columnColors[status]} min-h-[500px]`}>
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-semibold text-lg">{title}</h3>
        <span className="text-sm text-muted-foreground bg-white dark:bg-slate-900 px-2 py-1 rounded-full">
          {filteredTasks.length}
        </span>
      </div>

      <div className="flex-1 overflow-y-auto">
        {filteredTasks.length === 0 ? (
          <div className="text-center text-muted-foreground py-8">
            No tasks
          </div>
        ) : (
          filteredTasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              onMove={onMoveTask}
              onDelete={onDeleteTask}
              isHighlighted={task.id === highlightedTaskId}
            />
          ))
        )}
      </div>
    </div>
  )
}
