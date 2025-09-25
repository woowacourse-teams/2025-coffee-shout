import type { Meta, StoryObj } from '@storybook/react-webpack5';
import SelectionCard from './SelectionCard';
import AdeIcon from '@/assets/ade.svg';

const meta: Meta<typeof SelectionCard> = {
  title: 'Common/SelectionCard',
  component: SelectionCard,
  tags: ['autodocs'],
  parameters: {
    layout: 'centered',
  },
  argTypes: {
    color: {
      control: { type: 'color' },
      description: '카드의 배경색',
    },
    imageUrl: {
      control: { type: 'text' },
      description: '아이콘 이미지 경로',
    },
  },
  decorators: [
    (Story) => (
      <div style={{ width: '260px' }}>
        <Story />
      </div>
    ),
  ],
};

export default meta;

type Story = StoryObj<typeof SelectionCard>;

export const Default: Story = {
  args: {
    color: '#ffb2b2',
    imageUrl: AdeIcon,
    text: 'SelectionCard',
  },
};

export const NoIcon: Story = {
  args: {
    color: '#45B7D1',
    text: 'SelectionCard',
  },
};
