import RouletteIcon from '@/assets/roulette-icon.svg';
import StatisticsIcon from '@/assets/statistics-icon.svg';
import IconButton from '@/components/@common/IconButton/IconButton';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { useState } from 'react';
import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import * as S from './RouletteSection.styled';

type RouletteView = 'roulette' | 'statistics';

export const RouletteSection = () => {
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');
  const isRouletteView = currentView === 'roulette';

  const handleViewChange = () => {
    setCurrentView((prev) => (prev === 'roulette' ? 'statistics' : 'roulette'));
  };

  return (
    <>
      <SectionTitle title="룰렛" description="미니게임을 통해 당첨 확률이 조정됩니다" />
      <S.IconButtonWrapper>
        <IconButton
          iconSrc={isRouletteView ? StatisticsIcon : RouletteIcon}
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
