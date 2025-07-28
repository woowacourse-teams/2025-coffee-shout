import Button from '@/components/@common/Button/Button';
import Input from '@/components/@common/Input/Input';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { ChangeEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EnterRoomModal.styled';

type Props = {
  onClose: () => void;
};

const EnterRoomModal = ({ onClose }: Props) => {
  const navigate = useNavigate();
  const [joinCode, setJoinCode] = useState('');

  const handleEnter = () => {
    if (!joinCode.trim()) {
      alert('초대코드를 입력해주세요.');
      return;
    }

    // joinCode 유효한지 검증하는 로직 추가

    navigate(`/entry/name`, { state: { joinCode } });
    onClose();
  };

  return (
    <S.Container>
      <Paragraph>초대코드를 입력해주세요</Paragraph>
      <Input
        type="text"
        placeholder="ex) CODE1234"
        value={joinCode}
        onClear={() => setJoinCode('')}
        onChange={(e: ChangeEvent<HTMLInputElement>) => setJoinCode(e.target.value)}
        autoFocus
      />
      <S.ButtonContainer>
        <Button variant="secondary" onClick={onClose}>
          취소
        </Button>
        <Button variant="primary" onClick={handleEnter}>
          입장
        </Button>
      </S.ButtonContainer>
    </S.Container>
  );
};

export default EnterRoomModal;
