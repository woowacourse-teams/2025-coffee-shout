import type { Meta, StoryObj } from '@storybook/react-webpack5';
import MenuListItemSkeleton from './MenuListItemSkeleton';

const meta = {
  title: 'Composition/MenuListItemSkeleton',
  component: MenuListItemSkeleton,
  parameters: {
    layout: 'padded',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof MenuListItemSkeleton>;

export default meta;

type Story = StoryObj<typeof MenuListItemSkeleton>;

export const Default: Story = {
  render: () => <MenuListItemSkeleton />,
  parameters: {
    docs: {
      description: {
        story: '메뉴 리스트가 로딩 중일 때의 모습입니다.',
      },
    },
  },
};
