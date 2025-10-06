import Button from '@/components/@common/Button/Button';
import Input from '@/components/@common/Input/Input';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EnterRoomModal.styled';
import useLazyFetch from '@/apis/rest/useLazyFetch';

type JoinCodeCheckResponse = {
  exist: boolean;
};

type Props = {
  onClose: () => void;
};

const EnterRoomModal = ({ onClose }: Props) => {
  const navigate = useNavigate();
  const { joinCode, setJoinCode } = useIdentifier();

  const { execute: checkJoinCode } = useLazyFetch<JoinCodeCheckResponse>({
    endpoint: `/rooms/check-joinCode?joinCode=${joinCode}`,
    onSuccess: (data) => {
      if (!data.exist) {
        alert('참여코드가 유효한 방이 존재하지 않습니다.');
        return;
      }

      navigate(`/entry/name`);
      onClose();
    },
    onError: (error) => {
      // 추후 에러 바운더리에서 처리
      alert(error.message);
      setJoinCode('');
    },
  });

  const handleEnter = () => {
    if (!joinCode.trim()) {
      alert('초대코드를 입력해주세요.');
      return;
    }

    checkJoinCode();
  };

  const handleJoinCodeChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    const upperValue = value.toUpperCase();
    const lastChar = upperValue.slice(-1);

    const isTooLong = upperValue.length > 4;
    const isNotEmpty = upperValue.length > 0;
    const isInvalidChar = isNotEmpty && !/^[A-Z0-9]$/.test(lastChar);

    if (isTooLong || isInvalidChar) return;

    setJoinCode(value.toUpperCase());
  };

  return (
    <S.Container>
      <Paragraph>초대코드를 입력해주세요</Paragraph>
      <Input
        type="text"
        placeholder="4자리 영문과 숫자 조합 ex) AB12"
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
