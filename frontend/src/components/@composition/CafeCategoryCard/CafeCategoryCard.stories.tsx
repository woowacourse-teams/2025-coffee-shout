import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CafeCategoryCard from './CafeCategoryCard';
import CoffeeIcon from '@/assets/coffee.svg';
import SmoothieIcon from '@/assets/smoothie.svg';
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

export const Coffee: Story = {
  args: {
    imageUrl: CoffeeIcon,
    categoryName: '커피',
    onClick: () => alert('커피 카테고리 클릭!'),
    color: '#8B4513',
  },
  decorators: [
    (Story) => (
      <div style={{ width: '400px' }}>
        <Story />
      </div>
    ),
  ],
};

export const Smoothie: Story = {
  args: {
    imageUrl: SmoothieIcon,
    categoryName: '스무디',
    onClick: () => alert('스무디 카테고리 클릭!'),
    color: '#32CD32',
  },
  decorators: [
    (Story) => (
      <div style={{ width: '400px' }}>
        <Story />
      </div>
    ),
  ],
};

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
        imageUrl={CoffeeIcon}
        categoryName="커피"
        onClick={() => alert('커피 카테고리 클릭!')}
        color="#8B4513"
      />
      <CafeCategoryCard
        imageUrl={SmoothieIcon}
        categoryName="스무디"
        onClick={() => alert('스무디 카테고리 클릭!')}
        color="#32CD32"
      />
      <CafeCategoryCard
        imageUrl={AdeIcon}
        categoryName="에이드"
        onClick={() => alert('에이드 카테고리 클릭!')}
        color="#87CEEB"
      />
    </div>
  ),
};
