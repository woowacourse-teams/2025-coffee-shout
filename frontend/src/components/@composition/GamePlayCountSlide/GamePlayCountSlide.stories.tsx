import type { Meta, StoryObj } from '@storybook/react-webpack5';
import MiniGameStatsSlide from './MiniGameStatsSlide';

const meta: Meta<typeof MiniGameStatsSlide> = {
  title: 'Composition/MiniGameStatsSlide',
  component: MiniGameStatsSlide,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  decorators: [
    (Story) => (
      <div
        style={{
          background: '#ff6b6b',
          padding: '2rem',
          borderRadius: '20px',
          width: '375px',
          height: '400px',
        }}
      >
        <Story />
      </div>
    ),
  ],
  argTypes: {
    games: {
      control: 'object',
      description: '미니게임 통계 데이터',
    },
  },
  args: {
    games: [
      { name: '카드게임', count: 20 },
      { name: '레이싱게임', count: 15 },
    ],
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const SingleGame: Story = {
  args: {
    games: [{ name: '카드게임', count: 20 }],
  },
};

export const MultipleGames: Story = {
  args: {
    games: [
      { name: '카드게임', count: 20 },
      { name: '레이싱게임', count: 15 },
      { name: '룰렛게임', count: 10 },
    ],
  },
};
