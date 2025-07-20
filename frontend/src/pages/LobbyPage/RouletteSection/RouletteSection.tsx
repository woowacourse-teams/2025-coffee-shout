import React, { useState } from 'react';
import SwitchButton from '@/components/@common/SwitchButton/SwitchButton';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { RouletteWheel } from './RouletteWheel/RouletteWheel';
import { ProbabilityList } from './ProbabilityList/ProbabilityList';
import * as S from './RouletteSection.styled';

type RouletteView = 'roulette' | 'statistics';

export const RouletteSection = () => {
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');

  const handleViewChange = () => {
    setCurrentView(currentView === 'roulette' ? 'statistics' : 'roulette');
  };

  const renderContent = () => {
    switch (currentView) {
      case 'roulette':
        return <RouletteWheel />;
      case 'statistics':
        return <ProbabilityList />;
      default:
        return <RouletteWheel />;
    }
  };

  return (
    <>
      <SectionTitle title="룰렛" description="미니게임을 통해 당첨 확률이 조정됩니다" />
      <S.SwitchButtonWrapper>
        <SwitchButton targetView={currentView} onClick={handleViewChange} />
      </S.SwitchButtonWrapper>
      {renderContent()}
    </>
  );
};
