import type { Meta, StoryObj } from '@storybook/react-webpack5';
import { useState } from 'react';
import RouletteWheel from './RouletteWheel';
import { colorList } from '@/constants/color';

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
    probability: 15.0,
    playerColor: colorList[0],
  },
  {
    playerName: '김철수',
    probability: 12.0,
    playerColor: colorList[1],
  },
  {
    playerName: '이순신',
    probability: 18.0,
    playerColor: colorList[2],
  },
  {
    playerName: '박영희',
    probability: 10.0,
    playerColor: colorList[3],
  },
  {
    playerName: '정민수',
    probability: 14.0,
    playerColor: colorList[4],
  },
  {
    playerName: '최지영',
    probability: 11.0,
    playerColor: colorList[5],
  },
  {
    playerName: '강동원',
    probability: 8.0,
    playerColor: colorList[6],
  },
  {
    playerName: '윤서연',
    probability: 7.0,
    playerColor: colorList[7],
  },
  {
    playerName: '임태현',
    probability: 5.0,
    playerColor: colorList[8],
  },
];
