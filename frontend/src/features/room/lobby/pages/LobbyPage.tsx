import ShareIcon from '@/assets/share-icon.svg';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import useModal from '@/components/@common/Modal/useModal';
import ToggleButton from '@/components/@common/ToggleButton/ToggleButton';
import Layout from '@/layouts/Layout';
import { ReactElement, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import InviteCodeModal from '../components/InviteCodeModal/InviteCodeModal';
import { MiniGameSection } from '../components/MiniGameSection/MiniGameSection';
import { ParticipantSection } from '../components/ParticipantSection/ParticipantSection';
import { RouletteSection } from '../components/RouletteSection/RouletteSection';
import * as S from './LobbyPage.styled';

type UserRole = 'HOST' | 'GUEST';

type SectionType = '참가자' | '룰렛' | '미니게임';
type SectionComponents = {
  [K in SectionType]: ReactElement;
};

const SECTIONS: SectionComponents = {
  참가자: <ParticipantSection />,
  룰렛: <RouletteSection />,
  미니게임: <MiniGameSection />,
} as const;

const LobbyPage = () => {
  const navigate = useNavigate();
  const { openModal } = useModal();
  //TODO: Context로 빼기
  const [userRole, setUserRole] = useState<UserRole>('GUEST');

  const [currentSection, setCurrentSection] = useState<SectionType>('참가자');

  const handleClickBackButton = () => {
    navigate(-1);
  };

  const handleClickGameStartButton = () => {
    navigate('/room/:roomId/:miniGameId/ready');
  };

  const handleSectionChange = (option: SectionType) => {
    setCurrentSection(option);
  };

  const handleShare = () => {
    openModal(<InviteCodeModal />, {
      title: '초대 코드',
      showCloseButton: true,
    });
  };

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={handleClickBackButton} />} />
      <Layout.Content>
        <S.Container>
          {SECTIONS[currentSection]}
          <S.Wrapper>
            <ToggleButton
              options={['참가자', '룰렛', '미니게임']}
              selectedOption={currentSection}
              onSelectOption={handleSectionChange}
            />
          </S.Wrapper>
        </S.Container>
      </Layout.Content>
      <Layout.ButtonBar flexRatios={[5.5, 1]}>
        {userRole === 'HOST' ? (
          <Button variant="primary" onClick={handleClickGameStartButton}>
            게임 시작
          </Button>
        ) : (
          <Button variant="loading">게임 대기중</Button>
        )}
        <Button variant="primary" onClick={handleShare}>
          <img src={ShareIcon} alt="공유" />
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default LobbyPage;
