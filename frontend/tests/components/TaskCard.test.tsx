import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TaskCard } from '@/components/TaskCard'
import type { Task } from '@/types'

const mockTask: Task = {
  id: '1',
  boardId: 'board-1',
  title: 'Test Task',
  description: 'Test Description',
  status: 'TODO',
  priority: 'MEDIUM',
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:00Z',
}

describe('TaskCard', () => {
  const mockOnMove = vi.fn()
  const mockOnDelete = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render task title and description', () => {
    render(<TaskCard task={mockTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    expect(screen.getByText('Test Task')).toBeInTheDocument()
    expect(screen.getByText('Test Description')).toBeInTheDocument()
  })

  it('should render priority badge with correct color', () => {
    render(<TaskCard task={mockTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    const badge = screen.getByRole('status', { name: /priority: medium/i })
    expect(badge).toBeInTheDocument()
    expect(badge).toHaveClass('bg-yellow-500')
  })

  it('should render HIGH priority with red color', () => {
    const highPriorityTask = { ...mockTask, priority: 'HIGH' as const }
    render(<TaskCard task={highPriorityTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    const badge = screen.getByRole('status', { name: /priority: high/i })
    expect(badge).toHaveClass('bg-red-500')
  })

  it('should render LOW priority with green color', () => {
    const lowPriorityTask = { ...mockTask, priority: 'LOW' as const }
    render(<TaskCard task={lowPriorityTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    const badge = screen.getByRole('status', { name: /priority: low/i })
    expect(badge).toHaveClass('bg-green-500')
  })

  it('should call onMove with IN_PROGRESS when clicking right arrow on TODO task', async () => {
    const user = userEvent.setup()
    render(<TaskCard task={mockTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    const rightButton = screen.getByTitle('Move to next column')
    await user.click(rightButton)

    expect(mockOnMove).toHaveBeenCalledWith('1', 'IN_PROGRESS')
  })

  it('should disable left arrow for TODO tasks', () => {
    render(<TaskCard task={mockTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    const leftButton = screen.getByTitle('Move to previous column')
    expect(leftButton).toBeDisabled()
  })

  it('should call onMove with TODO when clicking left arrow on IN_PROGRESS task', async () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' as const }
    const user = userEvent.setup()
    render(<TaskCard task={inProgressTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    const leftButton = screen.getByTitle('Move to previous column')
    await user.click(leftButton)

    expect(mockOnMove).toHaveBeenCalledWith('1', 'TODO')
  })

  it('should disable right arrow for DONE tasks', () => {
    const doneTask = { ...mockTask, status: 'DONE' as const }
    render(<TaskCard task={doneTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    const rightButton = screen.getByTitle('Move to next column')
    expect(rightButton).toBeDisabled()
  })

  it('should call onDelete when clicking delete button', async () => {
    const user = userEvent.setup()
    render(<TaskCard task={mockTask} onMove={mockOnMove} onDelete={mockOnDelete} />)

    const deleteButton = screen.getByTitle('Delete task')
    await user.click(deleteButton)

    expect(mockOnDelete).toHaveBeenCalledWith('1')
  })

  it('should apply highlight animation when isHighlighted is true', () => {
    const { container } = render(
      <TaskCard task={mockTask} onMove={mockOnMove} onDelete={mockOnDelete} isHighlighted />
    )

    const card = container.firstChild
    expect(card).toHaveClass('animate-flash')
  })

  it('should not apply highlight animation when isHighlighted is false', () => {
    const { container } = render(
      <TaskCard task={mockTask} onMove={mockOnMove} onDelete={mockOnDelete} isHighlighted={false} />
    )

    const card = container.firstChild
    expect(card).not.toHaveClass('animate-flash')
  })
})
