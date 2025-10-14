import type { Meta, StoryObj } from '@storybook/react-webpack5';
import LowestProbabilitySlide from './LowestProbabilitySlide';

const meta: Meta<typeof LowestProbabilitySlide> = {
  title: 'Composition/LowestProbabilitySlide',
  component: LowestProbabilitySlide,
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
    winnerName: {
      control: 'text',
      description: '우승자 이름',
    },
    probability: {
      control: { type: 'range', min: 1, max: 100, step: 1 },
      description: '확률 (%)',
    },
  },
  args: {
    winnerName: '세라',
    probability: 5,
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const DifferentProbabilities: Story = {
  render: () => (
    <div
      style={{
        background: '#ff6b6b',
        padding: '2rem',
        borderRadius: '20px',
        width: '375px',
        height: '400px',
      }}
    >
      <LowestProbabilitySlide winnerName="세라" probability={1} />
    </div>
  ),
  parameters: {
    controls: { disable: true },
  },
};
