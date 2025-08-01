import Button from '@/components/@common/Button/Button';
import Input from '@/components/@common/Input/Input';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EnterRoomModal.styled';
import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';

type JoinCodeCheckResponse = {
  exist: boolean;
};

type Props = {
  onClose: () => void;
};

const EnterRoomModal = ({ onClose }: Props) => {
  const navigate = useNavigate();
  const { joinCode, setJoinCode } = useIdentifier();

  const handleEnter = async () => {
    if (!joinCode.trim()) {
      alert('초대코드를 입력해주세요.');
      return;
    }

    try {
      const { exist } = await api.get<JoinCodeCheckResponse>(
        `/rooms/check-joinCode?joinCode=${joinCode}`
      );

      if (!exist) {
        alert('참여코드가 유효한 방이 존재하지 않습니다.');
        return;
      }
    } catch (error) {
      if (error instanceof ApiError) {
        alert(error.message);
      } else if (error instanceof NetworkError) {
        alert('네트워크 연결을 확인해주세요');
      } else {
        alert('알 수 없는 오류가 발생했습니다');
      }

      setJoinCode('');
      return;
    }

    navigate(`/entry/name`);
    onClose();
  };

  const handleJoinCodeChange = (e: ChangeEvent<HTMLInputElement>) => {
    setJoinCode(e.target.value.toUpperCase());
  };

  return (
    <S.Container>
      <Paragraph>초대코드를 입력해주세요</Paragraph>
      <Input
        type="text"
        placeholder="ex) ABCDE"
        value={joinCode}
        onClear={() => setJoinCode('')}
        onChange={handleJoinCodeChange}
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
