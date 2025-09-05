import type { Meta, StoryObj } from '@storybook/react-webpack5';
import TemperatureToggle from './TemperatureToggle';

const meta: Meta<typeof TemperatureToggle> = {
  title: 'Common/TemperatureToggle',
  component: TemperatureToggle,
  parameters: {
    layout: 'centered',
  },
  decorators: [
    (Story) => (
      <div style={{ width: '250px' }}>
        <Story />
      </div>
    ),
  ],
  argTypes: {
    selectedTemperature: {
      control: { type: 'select' },
      options: ['HOT', 'ICE'],
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const HotSelected: Story = {
  args: {
    selectedTemperature: 'HOT',
  },
};

export const IcedSelected: Story = {
  args: {
    selectedTemperature: 'ICE',
  },
};
