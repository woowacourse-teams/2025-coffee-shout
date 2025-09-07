import type { Meta, StoryObj } from '@storybook/react-webpack5';
import JoinCodeModal from './JoinCodeModal';
import useModal from '@/components/@common/Modal/useModal';

const meta = {
  title: 'Common/JoinCodeModal',
  component: JoinCodeModal,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof JoinCodeModal>;

export default meta;

type Story = StoryObj<typeof JoinCodeModal>;

export const Default: Story = {
  render: () => {
    const { openModal } = useModal();

    const ModalContent = () => <JoinCodeModal onClose={() => {}} />;

    const handleOpen = () => {
      openModal(ModalContent());
    };

    return <button onClick={handleOpen}>JoinCode 모달 열기</button>;
  },
};
