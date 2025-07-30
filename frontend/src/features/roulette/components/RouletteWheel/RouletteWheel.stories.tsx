import { mockPlayers } from '@/features/room/lobby/components/RouletteSection/RouletteSection';
import RouletteWheel from './RouletteWheel';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import { useState } from 'react';

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
        <RouletteWheel isSpinning={isSpinning} players={mockPlayers} />
        <button onClick={handleSpin} disabled={isSpinning} style={{ marginTop: 16 }}>
          {isSpinning ? '돌아가는 중...' : '돌리기'}
        </button>
      </div>
    );
  },
};
