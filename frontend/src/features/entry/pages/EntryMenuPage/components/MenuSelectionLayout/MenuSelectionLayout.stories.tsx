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
    categorySelection: {
      color: '#ffb2b2',
      name: '아메리카노',
      imageUrl: '/path/to/coffee-icon.svg',
    },
    menuSelection: {
      color: '#b2d8ff',
      name: 'ICE',
    },
    showSelectedMenuCard: false,
  },
};

export const WithSelectedMenu: Story = {
  args: {
    categorySelection: {
      color: '#ffb2b2',
      name: '아메리카노',
      imageUrl: '/path/to/coffee-icon.svg',
    },
    menuSelection: {
      color: '#b2d8ff',
      name: 'ICE',
    },
    showSelectedMenuCard: true,
  },
};

export const WithChildren: Story = {
  args: {
    categorySelection: {
      color: '#ffb2b2',
      name: '아메리카노',
      imageUrl: '/path/to/coffee-icon.svg',
    },
    menuSelection: {
      color: '#b2d8ff',
      name: 'ICE',
    },
    showSelectedMenuCard: true,
    children: (
      <div style={{ padding: '16px', background: '#f0f0f0', borderRadius: '8px' }}>추가 컨텐츠</div>
    ),
  },
};
