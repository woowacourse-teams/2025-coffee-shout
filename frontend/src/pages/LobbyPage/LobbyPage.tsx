import React, { useState } from 'react';
import BackButton from '@/components/@common/BackButton/BackButton';
import Button from '@/components/@common/Button/Button';
import ToggleButton from '@/components/@common/ToggleButton/ToggleButton';
import Layout from '@/layouts/Layout';
import { ParticipantSection } from './ParticipantSection/ParticipantSection';
import { RouletteSection } from './RouletteSection/RouletteSection';
import { MiniGameSection } from './MiniGameSection/MiniGameSection';
import * as S from './LobbyPage.styled';

type SectionType = '참가자' | '룰렛' | '미니게임';

const LobbyPage = () => {
  const [currentSection, setCurrentSection] = useState<SectionType>('참가자');

  const handleClickButton = () => {
    console.log('게임 시작!');
  };

  const handleSectionChange = (option: string) => {
    setCurrentSection(option as SectionType);
  };

  const renderSection = () => {
    switch (currentSection) {
      case '참가자':
        return <ParticipantSection />;
      case '룰렛':
        return <RouletteSection />;
      case '미니게임':
        return <MiniGameSection />;
      default:
        return <ParticipantSection />;
    }
  };

  return (
    <Layout>
      <Layout.TopBar left={<BackButton onClick={() => {}} />} />
      <Layout.Content>
        <S.Container>
          {renderSection()}
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
        <Button variant="primary" onClick={handleClickButton}>
          게임 시작
        </Button>
        <Button variant="primary" onClick={handleClickButton}>
          <img src="/images/share-icon.svg" alt="공유" />
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default LobbyPage;
