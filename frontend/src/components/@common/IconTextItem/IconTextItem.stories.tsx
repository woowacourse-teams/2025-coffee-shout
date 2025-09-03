import type { Meta, StoryObj } from '@storybook/react-webpack5';
import { colorList } from '@/constants/color';
import IconTextItem from './IconTextItem';
import PlayerIcon from '../PlayerIcon/PlayerIcon';

const meta: Meta<typeof IconTextItem> = {
  title: 'Common/IconTextItem',
  component: IconTextItem,
  tags: ['autodocs'],
  argTypes: {
    gap: {
      control: { type: 'number', min: 0, max: 50, step: 2 },
    },
  },
  parameters: {
    layout: 'padded',
  },
};
export default meta;

type Story = StoryObj<typeof IconTextItem>;

export const Default: Story = {
  args: {
    iconContent: <PlayerIcon color={colorList[0]} />,
    textContent: <span>플레이어 이름</span>,
  },
  decorators: [
    (Story) => (
      <div style={{ width: '400px' }}>
        <Story />
      </div>
    ),
  ],
};

export const PlayerCardWithChildren: Story = {
  args: {
    iconContent: <PlayerIcon color={colorList[1]} />,
    textContent: (
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <h4 style={{ margin: 0, fontSize: '18px', fontWeight: 'bold' }}>김철수</h4>
      </div>
    ),
    gap: 20,
    rightContent: <h4 style={{ margin: 0, fontSize: '18px', fontWeight: 'bold' }}>10%</h4>,
  },
  decorators: [
    (Story) => (
      <div style={{ width: '400px' }}>
        <Story />
      </div>
    ),
  ],
};
