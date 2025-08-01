import type { Meta, StoryObj } from '@storybook/react-webpack5';
import { useState } from 'react';
import RouletteWheel from './RouletteWheel';

const meta: Meta<typeof RouletteWheel> = {
  title: 'Composition/RouletteWheel',
  component: RouletteWheel,
};

export default meta;

export const Interactive: StoryObj<typeof RouletteWheel> = {
  render: () => {
    const [isSpinning, setIsSpinning] = useState(false);
    const handleSpin = () => {
      if (isSpinning) return;
      setIsSpinning(true);
      setTimeout(() => setIsSpinning(false), 3000);
    };
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 24 }}>
        <RouletteWheel playerProbabilities={mockPlayerProbabilities} isSpinning={isSpinning} />
        <button onClick={handleSpin} disabled={isSpinning} style={{ marginTop: 16 }}>
          {isSpinning ? '돌아가는 중...' : '돌리기'}
        </button>
      </div>
    );
  },
};

const mockPlayerProbabilities = [
  {
    playerName: '홍길동',
    probability: 30.0,
  },
  {
    playerName: '김철수',
    probability: 30.0,
  },
  {
    playerName: '이순신',
    probability: 40.0,
  },
];
