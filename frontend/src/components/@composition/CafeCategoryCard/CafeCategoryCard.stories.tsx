import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CafeCategoryCard from './CafeCategoryCard';
import AdeIcon from '@/assets/ade.svg';

const meta: Meta<typeof CafeCategoryCard> = {
  title: 'Composition/CafeCategoryCard',
  component: CafeCategoryCard,
  tags: ['autodocs'],
  parameters: {
    layout: 'padded',
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Ade: Story = {
  args: {
    imageUrl: AdeIcon,
    categoryName: '에이드',
    onClick: () => alert('에이드 카테고리 클릭!'),
    color: '#87CEEB',
  },
  decorators: [
    (Story) => (
      <div style={{ width: '400px' }}>
        <Story />
      </div>
    ),
  ],
};

export const CategoryList: Story = {
  render: () => (
    <div style={{ width: '400px' }}>
      <CafeCategoryCard
        imageUrl={AdeIcon}
        categoryName="에이드"
        onClick={() => alert('에이드 카테고리 클릭!')}
        color="#87CEEB"
      />
    </div>
  ),
};
