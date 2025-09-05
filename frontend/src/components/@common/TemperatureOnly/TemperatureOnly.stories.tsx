import type { Meta, StoryObj } from '@storybook/react-webpack5';
import TemperatureOnly from './TemperatureOnly';

const meta: Meta<typeof TemperatureOnly> = {
  title: 'Common/TemperatureOnly',
  component: TemperatureOnly,
  parameters: {
    layout: 'centered',
  },
  argTypes: {
    temperature: {
      control: { type: 'select' },
      options: ['HOT', 'ICE'],
      description: '온도 옵션 (HOT 또는 ICED)',
    },
  },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const HotOnly: Story = {
  args: {
    temperature: 'HOT',
  },
  render: (args) => (
    <div style={{ width: '200px' }}>
      <TemperatureOnly {...args} />
    </div>
  ),
};

export const IcedOnly: Story = {
  args: {
    temperature: 'ICE',
  },
  render: (args) => (
    <div style={{ width: '200px' }}>
      <TemperatureOnly {...args} />
    </div>
  ),
};

export const AllVariants: Story = {
  render: () => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', width: '200px' }}>
      <TemperatureOnly temperature="HOT" />
      <TemperatureOnly temperature="ICE" />
    </div>
  ),
};
