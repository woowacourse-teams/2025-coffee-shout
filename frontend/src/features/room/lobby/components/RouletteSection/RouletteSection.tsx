import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import RouletteViewToggle from '@/components/@composition/RouletteViewToggle/RouletteViewToggle';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import ScreenReaderOnly from '@/components/@common/ScreenReaderOnly/ScreenReaderOnly';
import RouletteWheel from '@/features/roulette/components/RouletteWheel/RouletteWheel';
import { PlayerProbability, RouletteView } from '@/types/roulette';
import { useEffect, useState } from 'react';
import * as S from './RouletteSection.styled';

type Props = {
  playerProbabilities: PlayerProbability[];
};

const formatProbabilitiesForScreenReader = (playerProbabilities: PlayerProbability[]): string => {
  if (playerProbabilities.length === 0) {
    return '현재 참여한 인원이 없습니다.';
  }

  return playerProbabilities
    .map(({ playerName, probability }) => `${playerName}님의 확률 ${probability}%`)
    .join(', ');
};

export const RouletteSection = ({ playerProbabilities }: Props) => {
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');
  const [screenReaderMessage, setScreenReaderMessage] = useState<string>('');

  useEffect(() => {
    const initialMessage = `룰렛 화면입니다. 미니게임을 통해 당첨 확률이 조정됩니다. ${formatProbabilitiesForScreenReader(playerProbabilities)}`;
    setScreenReaderMessage(initialMessage);
  }, [playerProbabilities]);

  const handleViewChange = () => {
    setCurrentView((prev) => (prev === 'roulette' ? 'statistics' : 'roulette'));

    const viewChangeMessage = formatProbabilitiesForScreenReader(playerProbabilities);
    setScreenReaderMessage(viewChangeMessage);
  };

  return (
    <>
      <ScreenReaderOnly>{screenReaderMessage}</ScreenReaderOnly>
      <SectionTitle title="룰렛" description="미니게임을 통해 당첨 확률이 조정됩니다" />
      <S.IconButtonWrapper>
        <RouletteViewToggle currentView={currentView} onViewChange={handleViewChange} />
      </S.IconButtonWrapper>
      {renderContent(currentView, playerProbabilities)}
    </>
  );
};

const renderContent = (currentView: RouletteView, playerProbabilities: PlayerProbability[]) => {
  switch (currentView) {
    case 'statistics':
      return <ProbabilityList playerProbabilities={playerProbabilities} />;
    case 'roulette':
    default:
      return (
        <S.RouletteWheelWrapper>
          <RouletteWheel playerProbabilities={playerProbabilities} />
        </S.RouletteWheelWrapper>
      );
  }
};
