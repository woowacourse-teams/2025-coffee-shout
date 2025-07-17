import Headline3 from '@/components/@common/Headline3/Headline3';
import styled from '@emotion/styled';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import Modal from './Modal';
import useModal from './useModal';

const Button = styled.button`
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;

  &:hover {
    background-color: #0056b3;
  }
`;

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
`;

const Scroll = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;

  max-height: 200px;
  overflow-y: scroll;
  padding-right: 8px;
`;

const meta = {
  title: 'Features/UI/Modal',
  component: Modal,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof Modal>;

export default meta;

type Story = StoryObj<typeof Modal>;

export const Default: Story = {
  render: () => {
    const { openModal } = useModal();

    const ModalContent = () => (
      <p>옵션 없이 호출한 기본 모달입니다. 기본 모달은 닫기 버튼만 존재합니다.</p>
    );

    const handleOpen = () => {
      openModal(ModalContent());
    };

    return <Button onClick={handleOpen}>기본 모달</Button>;
  },
};

export const WithTitleAndCloseButton: Story = {
  render: () => {
    const { openModal } = useModal();

    const ModalContent = () => <p>제목과 닫기 버튼이 존재하는 모달입니다.</p>;

    const handleOpen = () => {
      openModal(ModalContent(), { title: '제목/닫기 있는 모달', showCloseButton: true });
    };

    return <Button onClick={handleOpen}>제목/닫기 있는 모달</Button>;
  },
};

export const WithoutTitle: Story = {
  render: () => {
    const { openModal, closeModal } = useModal();

    const ModalContent = () => {
      return (
        <ContentWrapper>
          <p>제목이 없는 모달입니다.</p>
          <Button onClick={closeModal}>확인</Button>
        </ContentWrapper>
      );
    };

    const handleOpen = () => {
      openModal(ModalContent(), { showCloseButton: true });
    };

    return <Button onClick={handleOpen}>제목 없는 모달</Button>;
  },
};

export const WithoutCloseButton: Story = {
  render: () => {
    const { openModal, closeModal } = useModal();

    const ModalContent = () => {
      return (
        <ContentWrapper>
          <p>닫기 버튼이 없는 모달입니다.</p>
          <Button onClick={closeModal}>확인</Button>
        </ContentWrapper>
      );
    };

    const handleOpen = () => {
      openModal(ModalContent(), { title: '닫기 버튼 없는 모달', showCloseButton: false });
    };

    return <Button onClick={handleOpen}>닫기 버튼 없는 모달</Button>;
  },
};

export const WithoutHeader: Story = {
  render: () => {
    const { openModal, closeModal } = useModal();

    const ModalContent = () => {
      return (
        <ContentWrapper>
          <Headline3>커스텀 헤더</Headline3>
          <p>Modal.Header를 사용하지 않은 모달입니다.</p>
          <Button onClick={closeModal}>닫기</Button>
        </ContentWrapper>
      );
    };

    const handleOpen = () => {
      openModal(ModalContent(), { hasHeader: false });
    };

    return <Button onClick={handleOpen}>헤더 없는 모달</Button>;
  },
};

export const WithScrollContent: Story = {
  render: () => {
    const { openModal, closeModal } = useModal();

    const ModalContent = () => {
      return (
        <ContentWrapper>
          <p>위쪽 고정 텍스트입니다.</p>
          <Scroll>
            {Array.from({ length: 30 }, (_, index) => (
              <p key={index}>스크롤 테스트용 텍스트입니다.</p>
            ))}
          </Scroll>
          <p>아래쪽 고정 텍스트입니다.</p>
          <Button onClick={closeModal}>닫기</Button>
        </ContentWrapper>
      );
    };

    const handleOpen = () => {
      openModal(ModalContent(), { title: '스크롤 모달', showCloseButton: true });
    };

    return <Button onClick={handleOpen}>스크롤 모달</Button>;
  },
};
