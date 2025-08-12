import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CardGameReadyPage from './CardGameReadyPage';

const meta = {
  title: 'Features/MiniGame/CardGame/CardGameReadyPage',
  component: CardGameReadyPage,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof CardGameReadyPage>;

export default meta;

type Story = StoryObj<typeof CardGameReadyPage>;

export const Default: Story = {
  render: () => {
    return <CardGameReadyPage />;
  },
  parameters: {
    layout: 'centered',
  },
};
