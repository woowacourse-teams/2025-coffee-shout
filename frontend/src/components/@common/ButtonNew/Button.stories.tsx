import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Button from './Button';

const meta: Meta<typeof Button> = {
  title: 'Common/Button',
  component: Button,
  tags: ['autodocs'],
};
export default meta;

type Story = StoryObj<typeof Button>;

export const Primary: Story = {
  args: {
    variant: 'primary',
    children: '게임 시작',
    width: '328px',
    height: '50px',
  },
};

export const Loading: Story = {
  args: {
    variant: 'loading',
    children: '게임 대기중',
    width: '328px',
    height: '50px',
  },
};

export const Secondary: Story = {
  args: {
    variant: 'secondary',
    children: '취소',
    width: '138px',
    height: '45px',
  },
};

export const PrimaryChange: Story = {
  args: {
    variant: 'primary',
    children: '변경',
    width: '138px',
    height: '45px',
  },
};

export const PrimaryWithIcon: Story = {
  args: {
    variant: 'primary',
    children: <img src={'/images/share-icon.svg'} alt="icon" />,
    width: '50px',
    height: '50px',
  },
};
