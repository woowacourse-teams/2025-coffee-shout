import type { Meta, StoryObj } from '@storybook/react-webpack5';
import ReadyOverlay from './ReadyOverlay';

const meta = {
  title: 'Features/MiniGame/CardGame/ReadyOverlay',
  component: ReadyOverlay,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof ReadyOverlay>;

export default meta;

type Story = StoryObj<typeof ReadyOverlay>;

export const Default: Story = {
  render: () => {
    return <ReadyOverlay />;
  },
};
