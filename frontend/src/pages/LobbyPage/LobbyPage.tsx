import ShareIcon from '@/assets/images/share-icon.svg';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import ToggleButton from '@/components/@common/ToggleButton/ToggleButton';
import Layout from '@/layouts/Layout';
import { ReactElement, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './LobbyPage.styled';
import { MiniGameSection } from './MiniGameSection/MiniGameSection';
import { ParticipantSection } from './ParticipantSection/ParticipantSection';
import { RouletteSection } from './RouletteSection/RouletteSection';

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
        <Button variant="primary" onClick={handleClickGameStartButton}>
          게임 시작
        </Button>
        <Button variant="primary" onClick={() => {}}>
          <img src={ShareIcon} alt="공유" />
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default LobbyPage;
