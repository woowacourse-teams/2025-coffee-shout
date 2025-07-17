import type { Meta, StoryObj } from '@storybook/react';
import ToggleButton from './ToggleButton';
import { useState } from 'react';

const meta: Meta<typeof ToggleButton> = {
  title: 'Common/ToggleButton',
  component: ToggleButton,
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof ToggleButton>;

export const Default: Story = {
  render: (args) => {
    const [selected, setSelected] = useState(args.selectedOption);
    return <ToggleButton {...args} selectedOption={selected} onSelectOption={setSelected} />;
  },
  args: {
    options: ['Option 1', 'Option 2', 'Option 3'],
    selectedOption: 'Option 1',
  },
};

export const TwoOptions: Story = {
  render: (args) => {
    const [selected, setSelected] = useState(args.selectedOption);
    return <ToggleButton {...args} selectedOption={selected} onSelectOption={setSelected} />;
  },
  args: {
    options: ['A', 'B'],
    selectedOption: 'A',
  },
};
