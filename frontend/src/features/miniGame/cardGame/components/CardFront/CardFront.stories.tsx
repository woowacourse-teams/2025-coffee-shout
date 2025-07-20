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

export const LongName: Story = {
  args: {
    size: 'large',
    onClick: () => {},
    player: {
      name: '매우매우매우매우매우긴이름입니다람쥐 ',
      iconSrc: '/images/profile-red.svg',
    },
  },
};

export const Grid: Story = {
  render: () => {
    const playerMap: Record<number, { name: string; iconSrc: string }> = {
      4: { name: '사용자명', iconSrc: '/images/profile-red.svg' },
      8: { name: '매우긴이름입니다', iconSrc: '/images/profile-red.svg' },
    };

    return (
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: '10px',
          placeItems: 'center',
        }}
      >
        {Array.from({ length: 9 }, (_, index) => (
          <CardFront key={index} onClick={() => {}} player={playerMap[index]} />
        ))}
      </div>
    );
  },
  parameters: {
    layout: 'centered',
  },
};
