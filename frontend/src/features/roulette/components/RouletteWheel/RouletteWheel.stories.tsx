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
    const [spinning, setSpinning] = useState(false);
    const handleSpin = () => {
      if (spinning) return;
      setSpinning(true);
      setTimeout(() => setSpinning(false), 3000);
    };
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 24 }}>
        <RouletteWheel spinning={spinning} />
        <button onClick={handleSpin} disabled={spinning} style={{ marginTop: 16 }}>
          {spinning ? '돌아가는 중...' : '돌리기'}
        </button>
      </div>
    );
  },
};
