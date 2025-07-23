import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CardFront, { IconColor } from './CardFront';

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
    card: { type: 'ADDITION', value: 10 },
  },
};

export const Medium: Story = {
  args: {
    size: 'medium',
    onClick: () => {},
    card: { type: 'MULTIPLIER', value: -1 },
  },
};

export const Large: Story = {
  args: {
    size: 'large',
    onClick: () => {},
    card: { type: 'ADDITION', value: 0 },
  },
};

export const WithPlayerSmall: Story = {
  args: {
    size: 'small',
    onClick: () => {},
    player: {
      name: '홍길동전',
      iconColor: 'red',
    },
    card: { type: 'MULTIPLIER', value: 0 },
  },
};

export const WithPlayerMedium: Story = {
  args: {
    size: 'medium',
    onClick: () => {},
    player: {
      name: '홍길동전',
      iconColor: 'red',
    },
    card: { type: 'MULTIPLIER', value: 2 },
  },
};

export const WithPlayerLarge: Story = {
  args: {
    size: 'large',
    onClick: () => {},
    player: {
      name: '홍길동전',
      iconColor: 'red',
    },
    card: { type: 'ADDITION', value: -40 },
  },
};

export const LongName: Story = {
  args: {
    size: 'large',
    onClick: () => {},
    player: {
      name: '매우매우매우매우매우긴이름입니다람쥐 ',
      iconColor: 'red',
    },
    card: { type: 'ADDITION', value: 10 },
  },
};

export const Grid: Story = {
  render: () => {
    const playerMap: Record<number, { name: string; iconColor: IconColor }> = {
      4: { name: '사용자명', iconColor: 'red' },
      8: { name: '매우긴이름입니다', iconColor: 'red' },
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
          <CardFront
            key={index}
            onClick={() => {}}
            player={playerMap[index]}
            card={{ type: 'MULTIPLIER', value: -1 }}
          />
        ))}
      </div>
    );
  },
  parameters: {
    layout: 'centered',
  },
};
