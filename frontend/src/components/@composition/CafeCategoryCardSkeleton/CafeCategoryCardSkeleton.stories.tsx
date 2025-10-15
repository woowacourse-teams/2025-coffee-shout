import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CafeCategoryCardSkeleton from './CafeCategoryCardSkeleton';

const meta = {
  title: 'Composition/CafeCategoryCardSkeleton',
  component: CafeCategoryCardSkeleton,
  parameters: {
    layout: 'padded',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof CafeCategoryCardSkeleton>;

export default meta;

type Story = StoryObj<typeof CafeCategoryCardSkeleton>;

export const Default: Story = {
  render: () => <CafeCategoryCardSkeleton />,
  parameters: {
    docs: {
      description: {
        story: '카테고리 카드가 로딩 중일 때의 모습입니다.',
      },
    },
  },
};
