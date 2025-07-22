import StatisticsIcon from '@/assets/statistics-icon.svg';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import IconButton from './IconButton';

const meta: Meta<typeof IconButton> = {
  title: 'Common/IconButton',
  component: IconButton,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    iconSrc: StatisticsIcon,
    onClick: () => console.log('IconButton clicked'),
  },
};
