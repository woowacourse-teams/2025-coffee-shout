import type { Meta, StoryObj } from '@storybook/react-webpack5';
import MenuSelectionLayout from './MenuSelectionLayout';

const meta: Meta<typeof MenuSelectionLayout> = {
  title: 'Features/Entry/MenuSelectionLayout',
  component: MenuSelectionLayout,
  parameters: {
    layout: 'centered',
  },
  decorators: [
    (Story) => (
      <div style={{ width: '400px' }}>
        <Story />
      </div>
    ),
  ],
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    title: '메뉴 선택',
    categorySelectionCard: {
      color: '#ffb2b2',
      text: '아메리카노',
      imageUrl: '/path/to/coffee-icon.svg',
    },
    menuSelectionCard: {
      color: '#b2d8ff',
      text: 'ICE',
    },
    showSelectedMenuCard: false,
    showChildren: false,
  },
};

export const WithSelectedMenu: Story = {
  args: {
    title: '메뉴 선택',
    categorySelectionCard: {
      color: '#ffb2b2',
      text: '아메리카노',
      imageUrl: '/path/to/coffee-icon.svg',
    },
    menuSelectionCard: {
      color: '#b2d8ff',
      text: 'ICE',
    },
    showSelectedMenuCard: true,
    showChildren: false,
  },
};

export const WithChildren: Story = {
  args: {
    title: '메뉴 선택',
    categorySelectionCard: {
      color: '#ffb2b2',
      text: '아메리카노',
      imageUrl: '/path/to/coffee-icon.svg',
    },
    menuSelectionCard: {
      color: '#b2d8ff',
      text: 'ICE',
    },
    showSelectedMenuCard: true,
    showChildren: true,
    children: (
      <div style={{ padding: '16px', background: '#f0f0f0', borderRadius: '8px' }}>추가 컨텐츠</div>
    ),
  },
};
