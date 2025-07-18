import type { Meta, StoryObj } from '@storybook/react-webpack5';
import GameActionButton from './GameActionButton';

const meta: Meta<typeof GameActionButton> = {
  title: 'Common/GameActionButton',
  component: GameActionButton,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  argTypes: {
    onClick: { action: 'clicked' },
    isSelected: {
      control: 'boolean',
      description: '버튼의 선택 상태를 나타냅니다',
    },
    gameName: {
      control: 'text',
      description: '게임 이름을 설정합니다',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    isSelected: false,
    gameName: 'Coffee Shout',
  },
};

export const Selected: Story = {
  args: {
    isSelected: true,
    gameName: 'Coffee Shout',
  },
};
