import type { Meta, StoryObj } from '@storybook/react-webpack5';
import MenuListItem from './MenuListItem';

const meta: Meta<typeof MenuListItem> = {
  title: 'Common/MenuListItem',
  component: MenuListItem,
  tags: ['autodocs'],
  parameters: {
    layout: 'centered',
  },
  argTypes: {
    text: {
      control: { type: 'text' },
      description: '메뉴 아이템 텍스트',
    },
  },
  decorators: [
    (Story) => (
      <div style={{ width: '300px' }}>
        <Story />
      </div>
    ),
  ],
};

export default meta;

type Story = StoryObj<typeof MenuListItem>;

export const Americano: Story = {
  args: {
    text: '아메리카노',
  },
};

export const Cappuccino: Story = {
  args: {
    text: '카푸치노',
  },
};

export const Latte: Story = {
  args: {
    text: '카페 라떼',
  },
};
