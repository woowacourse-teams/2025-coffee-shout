import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CardFront from './CardFront';

const meta = {
  title: 'Features/MiniGame/CardGame/CardFront',
  component: CardFront,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof CardFront>;

export default meta;

type Story = StoryObj<typeof CardFront>;

export const Small: Story = {
  args: {
    size: 'small',
    onClick: () => {},
  },
};

export const Medium: Story = {
  args: {
    size: 'medium',
    onClick: () => {},
  },
};

export const Large: Story = {
  args: {
    size: 'large',
    onClick: () => {},
  },
};

export const WithPlayerSmall: Story = {
  args: {
    size: 'small',
    onClick: () => {},
    player: {
      name: '홍길동전',
      iconSrc: '/images/profile-red.svg',
    },
  },
};

export const WithPlayerMedium: Story = {
  args: {
    size: 'medium',
    onClick: () => {},
    player: {
      name: '홍길동전',
      iconSrc: '/images/profile-red.svg',
    },
  },
};

export const WithPlayerLarge: Story = {
  args: {
    size: 'large',
    onClick: () => {},
    player: {
      name: '홍길동전',
      iconSrc: '/images/profile-red.svg',
    },
  },
};
