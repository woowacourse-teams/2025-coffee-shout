import type { Meta, StoryObj } from '@storybook/react-webpack5';
import { useEffect, useState } from 'react';
import CircularProgress from './CircularProgress';

const meta: Meta<typeof CircularProgress> = {
  title: 'Features/MiniGame/CardGame/CircularProgress',
  component: CircularProgress,
  parameters: {
    layout: 'centered',
  },
  argTypes: {
    current: {
      control: { type: 'number', min: 0, max: 10 },
      description: '현재 카운트',
    },
    total: {
      control: { type: 'number', min: 1, max: 10 },
      description: '전체 카운트',
    },
    size: {
      control: { type: 'text' },
      description: 'Progress 크기 (rem 단위, 예: "2rem")',
    },
  },
  tags: ['autodocs'],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const FullProgress: Story = {
  args: {
    current: 0,
    total: 10,
  },
};

export const NoProgress: Story = {
  args: {
    current: 10,
    total: 10,
  },
};

export const CountdownAnimation: Story = {
  render: () => {
    const [current, setCurrent] = useState(10);

    useEffect(() => {
      if (current > 0) {
        const timer = setTimeout(() => setCurrent(current - 1), 1_000);
        return () => clearTimeout(timer);
      }
    }, [current]);

    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
        <CircularProgress current={current} total={10} />
        <button onClick={() => setCurrent(10)}>리셋 버튼</button>
      </div>
    );
  },
};
