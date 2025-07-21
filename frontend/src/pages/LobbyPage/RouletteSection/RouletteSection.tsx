import { useState } from 'react';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import RouletteWheel from '../../../components/@composition/RouletteWheel/RouletteWheel';
import ProbabilityList from '../../../components/@composition/ProbabilityList/ProbabilityList';
import IconButton from '@/components/@common/IconButton/IconButton';
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
          iconSrc={isRouletteView ? '/images/statistics-icon.svg' : '/images/roulette-icon.svg'}
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
