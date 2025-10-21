import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CafeCategoryCard from './CafeCategoryCard';
import CoffeeCharacterIcon from '@/assets/coffee-character.svg';
import CustomMenuIcon from '@/assets/custom-menu-icon.svg';

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

export const CoffeeCategory: Story = {
  render: () => (
    <div style={{ width: '400px' }}>
      <CafeCategoryCard
        imageUrl={CoffeeCharacterIcon}
        categoryName="아메리카노"
        onClick={() => alert('아메리카노 카테고리 클릭!')}
        color="#8B4513"
      />
    </div>
  ),
};

export const CustomMenuCategory: Story = {
  render: () => (
    <div style={{ width: '400px' }}>
      <CafeCategoryCard
        imageUrl={CustomMenuIcon}
        categoryName="커스텀 메뉴"
        onClick={() => alert('커스텀 메뉴 카테고리 클릭!')}
        color="#FF6B6B"
      />
    </div>
  ),
};
