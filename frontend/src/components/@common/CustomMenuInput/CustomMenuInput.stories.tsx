import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CustomMenuInput from './CustomMenuInput';

const meta: Meta<typeof CustomMenuInput> = {
  title: 'Common/CustomMenuInput',
  component: CustomMenuInput,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    placeholder: '커스텀 메뉴 입력',
  },
  decorators: [
    (Story) => (
      <div style={{ width: '200px' }}>
        <Story />
      </div>
    ),
  ],
};

export const WithValue: Story = {
  args: {
    placeholder: '커스텀 메뉴 입력',
    value: '아메리카노',
  },
  decorators: [
    (Story) => (
      <div style={{ width: '200px' }}>
        <Story />
      </div>
    ),
  ],
  parameters: {
    docs: {
      description: {
        story: '값이 입력된 상태입니다. Done 버튼이 포인트 색상으로 표시됩니다.',
      },
    },
  },
};

export const EmptyValue: Story = {
  args: {
    placeholder: '커스텀 메뉴 입력',
    value: '',
  },
  decorators: [
    (Story) => (
      <div style={{ width: '200px' }}>
        <Story />
      </div>
    ),
  ],
  parameters: {
    docs: {
      description: {
        story: '값이 비어있는 상태입니다. Done 버튼이 회색으로 표시됩니다.',
      },
    },
  },
};
