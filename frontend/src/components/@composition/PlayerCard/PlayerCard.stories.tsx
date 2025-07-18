import Headline4 from '@/components/@common/Headline4/Headline4';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import PlayerCard from './PlayerCard';

const meta = {
  title: 'Composition/PlayerCard',
  component: PlayerCard,
  tags: ['autodocs'],
} satisfies Meta<typeof PlayerCard>;

export default meta;

type Story = StoryObj<typeof PlayerCard>;

export const WithText: Story = {
  args: {
    name: '홍길동',
    iconSrc: '/images/profile-red.svg',
    children: <Headline4>10%</Headline4>,
  },
};

export const WithIcon: Story = {
  args: {
    name: '김철수',
    iconSrc: '/images/profile-red.svg',
    children: <img src="/images/juice.svg" alt="juice" />,
  },
};

export const MultipleCards: Story = {
  render: () => (
    <>
      {Array.from({ length: 6 }, (_, index) => (
        <PlayerCard key={index} name="이영희" iconSrc="/images/profile-red.svg">
          <Headline4>20점</Headline4>
        </PlayerCard>
      ))}
    </>
  ),
};
