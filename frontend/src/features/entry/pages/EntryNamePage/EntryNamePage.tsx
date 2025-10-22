import useLazyFetch from '@/apis/rest/useLazyFetch';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import Headline3 from '@/components/@common/Headline3/Headline3';
import Input from '@/components/@common/Input/Input';
import ProgressCounter from '@/components/@common/ProgressCounter/ProgressCounter';
import useToast from '@/components/@common/Toast/useToast';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { useReplaceNavigate } from '@/hooks/useReplaceNavigate';
import Layout from '@/layouts/Layout';
import { useRef, useState } from 'react';
import * as S from './EntryNamePage.styled';

const MAX_NAME_LENGTH = 10;

type PlayerNameCheckResponse = {
  exist: boolean;
};

const EntryNamePage = () => {
  const [name, setName] = useState('');
  const navigate = useReplaceNavigate();
  const { setMyName, joinCode } = useIdentifier();
  const { playerType } = usePlayerType();
  const { showToast } = useToast();
  const buttonRef = useRef<HTMLButtonElement>(null);

  const { execute: checkGuestName } = useLazyFetch<PlayerNameCheckResponse>({
    endpoint: `/rooms/check-guestName?joinCode=${joinCode}&guestName=${name}`,
  });

  const handleNavigateToHome = () => {
    navigate('/');
  };

  const handleNavigateToMenu = async () => {
    if (playerType === 'GUEST') {
      const response = await checkGuestName();
      if (!response) return;
      if (response.exist) {
        showToast({
          type: 'error',
          message: '중복된 닉네임이 존재합니다. 새로운 닉네임을 입력해주세요.',
        });
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
              autoFocus
              onKeyDown={(e) => {
                if (e.key === 'Enter' && name.length > 0) {
                  buttonRef.current?.focus();
                }
              }}
            />
            <S.ProgressWrapper>
              <ProgressCounter
                current={name.length}
                total={MAX_NAME_LENGTH}
                ariaLabel={`${MAX_NAME_LENGTH}글자 중 ${name.length}글자 입력하였습니다`}
              />
            </S.ProgressWrapper>
          </S.Wrapper>
        </S.Container>
      </Layout.Content>
      <Layout.ButtonBar>
        <Button
          ref={buttonRef}
          variant={isButtonDisabled ? 'disabled' : 'primary'}
          onClick={handleNavigateToMenu}
        >
          메뉴 선택하러 가기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default EntryNamePage;
