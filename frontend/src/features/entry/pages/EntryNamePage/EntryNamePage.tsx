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

const MAX_NAME_LENGTH = 10;

const EntryNamePage = () => {
  const [name, setName] = useState('');
  const navigate = useNavigate();
  const { setMyName } = useIdentifier();

  const handleNavigateToHome = () => navigate('/');
  const handleNavigateToMenu = () => {
    // TODO: 이름 유효성 검증 먼저 진행
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
