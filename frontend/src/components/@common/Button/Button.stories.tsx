import ShareIcon from '@/assets/images/share-icon.svg';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Button from './Button';

const meta: Meta<typeof Button> = {
  title: 'Common/Button',
  component: Button,
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: { type: 'select' },
      options: ['primary', 'secondary', 'disabled', 'loading'],
    },
    height: {
      control: { type: 'select' },
      options: ['small', 'medium', 'large'],
    },
  },
};
export default meta;

type Story = StoryObj<typeof Button>;

export const Primary: Story = {
  args: {
    variant: 'primary',
    children: 'Primary',
    width: '120px',
    height: 'large',
  },
};

export const Secondary: Story = {
  args: {
    variant: 'secondary',
    children: 'Secondary',
    width: '120px',
    height: 'large',
  },
};

export const Disabled: Story = {
  args: {
    variant: 'disabled',
    children: 'Disabled',
    width: '120px',
    height: 'large',
  },
};

export const Loading: Story = {
  args: {
    variant: 'loading',
    children: 'Loading',
    width: '120px',
    height: 'large',
  },
};

export const IconButton: Story = {
  args: {
    variant: 'primary',
    children: <img src={ShareIcon} alt="icon" />,
    width: '40px',
    height: 'small',
  },
};
