import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import Headline3 from '@/components/@common/Headline3/Headline3';
import Input from '@/components/@common/Input/Input';
import ProgressCounter from '@/components/@common/ProgressCounter/ProgressCounter';
import Layout from '@/layouts/Layout';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EntryNamePage.styled';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';

const MAX_NAME_LENGTH = 10;

type NickNameCheckResponse = {
  exist: boolean;
};

const EntryNamePage = () => {
  const [name, setName] = useState('');
  const navigate = useNavigate();
  const { joinCode, setMyName } = useIdentifier();
  const { playerType } = usePlayerType();

  const handleNavigateToHome = () => {
    navigate('/');
  };

  const handleNavigateToMenu = async () => {
    if (name.length === 0) {
      alert('닉네임을 입력해주세요.');
      return;
    }

    if (playerType === 'GUEST') {
      try {
        const { exist } = await api.get<NickNameCheckResponse>(
          `/rooms/check-guestName?joinCode=${joinCode}&guestName=${name}`
        );

        if (exist) {
          alert('이미 존재하는 플레이어 닉네임입니다.');
          setMyName('');
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
      }
    }

    setMyName(name);
    navigate('/entry/menu');
  };

  const isButtonDisabled = name.length === 0;

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleNavigateToHome} />} />
      <Layout.Content>
        <S.Container>
          <Headline3>닉네임을 입력해주세요</Headline3>
          <S.Wrapper>
            <Input
              value={name}
              onChange={(e) => setName(e.target.value)}
              onClear={() => setName('')}
              placeholder="닉네임을 입력해주세요"
              maxLength={MAX_NAME_LENGTH}
            />
            <S.ProgressWrapper>
              <ProgressCounter current={name.length} total={MAX_NAME_LENGTH} />
            </S.ProgressWrapper>
          </S.Wrapper>
        </S.Container>
      </Layout.Content>
      <Layout.ButtonBar>
        <Button variant={isButtonDisabled ? 'disabled' : 'primary'} onClick={handleNavigateToMenu}>
          메뉴 선택하러 가기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default EntryNamePage;
