import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Divider from './Divider';

const meta: Meta<typeof Divider> = {
  title: 'Common/Divider',
  component: Divider,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  argTypes: {
    color: {
      control: 'color',
      description: '구분선 색상',
    },
    height: {
      control: 'text',
      description: '구분선 높이',
    },

    width: {
      control: 'text',
      description: '구분선 너비',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};

export const CustomColor: Story = {
  args: {
    color: '#ff0000',
  },
};

export const ThickDivider: Story = {
  args: {
    height: '4px',
  },
};

export const NarrowWidth: Story = {
  args: {
    width: '50%',
  },
};
