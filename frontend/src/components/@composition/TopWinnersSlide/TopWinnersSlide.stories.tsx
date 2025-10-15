import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Top3WinnersSlide from './Top3WinnersSlide';

const meta: Meta<typeof Top3WinnersSlide> = {
  title: 'Composition/Top3WinnersSlide',
  component: Top3WinnersSlide,
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
    winners: {
      control: 'object',
      description: 'TOP3 당첨자 데이터',
    },
  },
  args: {
    winners: [
      { name: '세라', count: 20 },
      { name: '민수', count: 15 },
      { name: '지영', count: 12 },
    ],
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const LessThanThree: Story = {
  args: {
    winners: [
      { name: '세라', count: 20 },
      { name: '민수', count: 15 },
    ],
  },
};

export const SingleWinner: Story = {
  args: {
    winners: [{ name: '세라', count: 20 }],
  },
};
