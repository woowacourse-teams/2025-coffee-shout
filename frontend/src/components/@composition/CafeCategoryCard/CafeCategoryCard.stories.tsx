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
    iconSrc: CoffeeIcon,
    categoryName: '커피',
    onClick: () => alert('커피 카테고리 클릭!'),
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
    iconSrc: SmoothieIcon,
    categoryName: '스무디',
    onClick: () => alert('스무디 카테고리 클릭!'),
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
    iconSrc: AdeIcon,
    categoryName: '에이드',
    onClick: () => alert('에이드 카테고리 클릭!'),
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
        iconSrc={CoffeeIcon}
        categoryName="커피"
        onClick={() => alert('커피 카테고리 클릭!')}
      />
      <CafeCategoryCard
        iconSrc={SmoothieIcon}
        categoryName="스무디"
        onClick={() => alert('스무디 카테고리 클릭!')}
      />
      <CafeCategoryCard
        iconSrc={AdeIcon}
        categoryName="에이드"
        onClick={() => alert('에이드 카테고리 클릭!')}
      />
    </div>
  ),
};
