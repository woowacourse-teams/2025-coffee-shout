import type { Meta, StoryObj } from '@storybook/react-webpack5';
import CustomMenuButton from './CustomMenuButton';

const meta: Meta<typeof CustomMenuButton> = {
  title: 'Common/CustomMenuButton',
  component: CustomMenuButton,
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    onClick: () => alert('직접 입력 버튼이 클릭되었습니다!'),
  },
  decorators: [
    (Story) => (
      <div
        style={{
          width: '375px',
          height: '400px',
          backgroundColor: '#f5f5f5',
          position: 'relative',
          margin: '0 auto',
        }}
      >
        <Story />
      </div>
    ),
  ],
};
