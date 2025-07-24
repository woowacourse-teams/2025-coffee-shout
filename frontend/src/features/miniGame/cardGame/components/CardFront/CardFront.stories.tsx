import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CardFront from './CardFront';
import { IconColor } from '@/types/player';
import CardBack from '../CardBack/CardBack';

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
    card: { type: 'ADDITION', value: 10 },
  },
};

export const Medium: Story = {
  args: {
    size: 'medium',
    card: { type: 'MULTIPLIER', value: -1 },
  },
};

export const Large: Story = {
  args: {
    size: 'large',
    card: { type: 'ADDITION', value: 0 },
  },
};

export const WithPlayerSmall: Story = {
  args: {
    size: 'small',
    playerIconColor: 'red',
    card: { type: 'MULTIPLIER', value: 0 },
  },
};

export const WithPlayerMedium: Story = {
  args: {
    size: 'medium',
    playerIconColor: 'red',
    card: { type: 'MULTIPLIER', value: 2 },
  },
};

export const WithPlayerLarge: Story = {
  args: {
    size: 'large',
    playerIconColor: 'red',
    card: { type: 'ADDITION', value: -40 },
  },
};

export const Grid: Story = {
  render: () => {
    const playerIconColorMap = [
      'red' as IconColor,
      'red' as IconColor,
      undefined,
      undefined,
      undefined,
      'red' as IconColor,
      undefined,
      'red' as IconColor,
      'red' as IconColor,
    ];

    return (
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: '10px',
          placeItems: 'center',
        }}
      >
        {Array.from({ length: 9 }, (_, index) =>
          playerIconColorMap[index] ? (
            <CardFront
              key={index}
              playerIconColor={playerIconColorMap[index]}
              card={{ type: 'MULTIPLIER', value: -1 }}
            />
          ) : (
            <CardBack key={index} />
          )
        )}
      </div>
    );
  },
  parameters: {
    layout: 'centered',
  },
};
