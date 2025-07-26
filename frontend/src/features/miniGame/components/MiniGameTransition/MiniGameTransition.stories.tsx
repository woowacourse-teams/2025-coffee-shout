import type { Meta, StoryObj } from '@storybook/react-webpack5';
import MiniGameTransition from './MiniGameTransition';
import CardsStackIcon from '@/assets/card-stack-icon.svg';
import styled from '@emotion/styled';
import { keyframes } from '@emotion/react';

const meta = {
  title: 'composition/MiniGameTransition',
  component: MiniGameTransition,

  tags: ['autodocs'],
} satisfies Meta<typeof MiniGameTransition>;

export default meta;

type Story = StoryObj<typeof MiniGameTransition>;

export const Default: Story = {
  args: {
    prevRound: 2,
    children: <img src={CardsStackIcon} alt="cards" />,
  },
  render: (args) => (
    <RootContainer>
      <MiniGameTransition {...args} />
    </RootContainer>
  ),
};

export const Animated: Story = {
  args: {
    prevRound: 1,
    children: <img src={CardsStackIcon} alt="cards" />,
  },
  render: (args) => (
    <AnimatedContainer>
      <MiniGameTransition {...args} />
    </AnimatedContainer>
  ),
};

const RootContainer = styled.div`
  max-width: 430px;
  width: 100%;
  height: 100dvh;
  margin: 0 auto;
`;

const slideInPauseOut = keyframes`
  0% {
    transform: translateX(100vw);
    opacity: 0;
  }
  10% {
    transform: translateX(0);
    opacity: 1;
  }
  90% {
    transform: translateX(0);
    opacity: 1;
  }
  95% {
    transform: translateX(-100vw);
    opacity: 0;
  }
  100% {
    transform: translateX(-100vw);
    opacity: 0;
  }
`;

const AnimatedContainer = styled.div`
  max-width: 430px;
  width: 100%;
  height: 100dvh;
  margin: 0 auto;
  animation: ${slideInPauseOut} 2.5s cubic-bezier(0.77, 0, 0.175, 1) both;
  animation-delay: 2s;
`;
