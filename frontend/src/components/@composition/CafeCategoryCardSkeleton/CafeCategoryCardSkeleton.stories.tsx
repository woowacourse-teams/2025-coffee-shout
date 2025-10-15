import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CafeCategoryCardSkeleton from './CafeCategoryCardSkeleton';

const meta = {
  title: 'Components/Composition/CafeCategoryCardSkeleton',
  component: CafeCategoryCardSkeleton,
  parameters: {
    layout: 'padded',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof CafeCategoryCardSkeleton>;

export default meta;

type Story = StoryObj<typeof CafeCategoryCardSkeleton>;

export const Default: Story = {};

export const Multiple: Story = {
  render: () => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0' }}>
      <CafeCategoryCardSkeleton />
      <CafeCategoryCardSkeleton />
      <CafeCategoryCardSkeleton />
      <CafeCategoryCardSkeleton />
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: '여러 개의 스켈레톤이 로딩 중일 때의 모습입니다.',
      },
    },
  },
};
