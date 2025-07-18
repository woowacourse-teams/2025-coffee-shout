import type { Meta, StoryObj } from '@storybook/react-webpack5';
import SwitchButton from './SwitchButton';

const meta: Meta<typeof SwitchButton> = {
  title: 'Common/SwitchButton',
  component: SwitchButton,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    currentView: 'statistics',
    onClick: () => console.log('Switch to roulette'),
  },
};

export const Roulette: Story = {
  args: {
    currentView: 'roulette',
    onClick: () => console.log('Switch to statistics'),
  },
};
