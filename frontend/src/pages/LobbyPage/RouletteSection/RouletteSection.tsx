import { useState } from 'react';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { RouletteWheel } from './RouletteWheel/RouletteWheel';
import { ProbabilityList } from './ProbabilityList/ProbabilityList';
import * as S from './RouletteSection.styled';
import IconButton from '@/components/@common/IconButton/IconButton';

type RouletteView = 'roulette' | 'statistics';

export const RouletteSection = () => {
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');

  const handleViewChange = () => {
    setCurrentView(currentView === 'roulette' ? 'statistics' : 'roulette');
  };

  return (
    <>
      <SectionTitle title="룰렛" description="미니게임을 통해 당첨 확률이 조정됩니다" />
      <S.IconButtonWrapper>
        <IconButton
          iconSrc={
            currentView === 'roulette' ? '/images/statistics-icon.svg' : '/images/roulette-icon.svg'
          }
          onClick={handleViewChange}
        />
      </S.IconButtonWrapper>
      {renderContent(currentView)}
    </>
  );
};

const renderContent = (currentView: RouletteView) => {
  switch (currentView) {
    case 'statistics':
      return <ProbabilityList />;
    case 'roulette':
    default:
      return <RouletteWheel />;
  }
};
