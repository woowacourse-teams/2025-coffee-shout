import { api } from '@/apis/rest/api';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import Headline3 from '@/components/@common/Headline3/Headline3';
import Input from '@/components/@common/Input/Input';
import ProgressCounter from '@/components/@common/ProgressCounter/ProgressCounter';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './EntryNamePage.styled';

const MAX_NAME_LENGTH = 10;

type PlayerNameCheckResponse = {
  exist: boolean;
};

const EntryNamePage = () => {
  const [name, setName] = useState('');
  const navigate = useNavigate();
  const { setMyName, joinCode } = useIdentifier();
  const { playerType } = usePlayerType();

  const handleNavigateToHome = () => {
    navigate('/');
  };

  const handleNavigateToMenu = async () => {
    if (playerType === 'GUEST') {
      const { exist } = await api.get<PlayerNameCheckResponse>(
        `/rooms/check-guestName?joinCode=${joinCode}&guestName=${name}`
      );

      if (exist) {
        alert('중복된 닉네임이 존재합니다. 새로운 닉네임을 입력해주세요.');
        return;
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
        <Button
          variant={isButtonDisabled ? 'disabled' : 'primary'}
          onClick={handleNavigateToMenu}
          height="large"
        >
          메뉴 선택하러 가기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default EntryNamePage;
