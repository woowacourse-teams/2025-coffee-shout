import type { Meta, StoryObj } from '@storybook/react-webpack5';
import RankItem from './RankItem';
import { ReactNode } from 'react';

const BlueBackground = ({ children }: { children: ReactNode }) => (
  <div style={{ backgroundColor: '#354557', padding: '20px', borderRadius: '8px' }}>{children}</div>
);

const meta: Meta<typeof RankItem> = {
  title: 'Features/MiniGame/RacingGame/RankItem/RankItem',
  component: RankItem,
  parameters: {
    layout: 'centered',
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => (
    <BlueBackground>
      <RankItem playerName="Player1" rank={1} isMe={false} isFixed={false} />
    </BlueBackground>
  ),
};

export const Fixed: Story = {
  render: () => (
    <BlueBackground>
      <RankItem playerName="Player1" rank={1} isMe={false} isFixed={true} />
    </BlueBackground>
  ),
};
