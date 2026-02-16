import { useState } from 'react'
import { BoardList } from '@/components/BoardList'
import { TaskBoard } from '@/components/TaskBoard'
import type { Board } from '@/types'

function App() {
  const [selectedBoard, setSelectedBoard] = useState<Board | null>(null)

  if (selectedBoard) {
    return (
      <TaskBoard
        board={selectedBoard}
        onBack={() => setSelectedBoard(null)}
      />
    )
  }

  return <BoardList onSelectBoard={setSelectedBoard} />
}

export default App
