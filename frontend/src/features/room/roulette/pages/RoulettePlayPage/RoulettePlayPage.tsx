import Button from '@/components/@common/Button/Button';
import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import RoulettePlaySection from '../../components/RoulettePlaySection/RoulettePlaySection';
import * as S from './RoulettePlayPage.styled';
import useRouletteProbabilities from './hooks/useRouletteProbabilities';
import useRoulettePlay from './hooks/useRoulettePlay';
import { RouletteView, RouletteWinnerResponse } from '@/types/roulette';
import { useCallback, useState } from 'react';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import RouletteViewToggle from '@/components/@composition/RouletteViewToggle/RouletteViewToggle';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';

const RoulettePlayPage = () => {
  const { joinCode } = useIdentifier();
  const { playerType } = usePlayerType();
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');
  const { winner, randomAngle, isSpinning, handleSpinClick, startSpinWithResult } =
    useRoulettePlay();
  const { probabilityHistory } = useProbabilityHistory();

  const { isLoading: isProbabilitiesLoading } = useRouletteProbabilities();

  const handleWinnerData = useCallback(
    (data: RouletteWinnerResponse) => {
      setCurrentView('roulette');
      startSpinWithResult(data);
    },
    [startSpinWithResult]
  );

  useWebSocketSubscription<RouletteWinnerResponse>(`/room/${joinCode}/winner`, handleWinnerData);

  const handleViewChange = () => {
    setCurrentView((prev) => (prev === 'statistics' ? 'roulette' : 'statistics'));
  };

  const VIEW_COMPONENTS = {
    roulette: (
      <RoulettePlaySection
        isSpinning={isSpinning}
        winner={winner}
        randomAngle={randomAngle}
        isProbabilitiesLoading={isProbabilitiesLoading}
      />
    ),
    statistics: <ProbabilityList playerProbabilities={probabilityHistory.current} />,
  };

  //TODO: 다른 에러 처리방식을 찾아보기
  if (!playerType) return null;

  return (
    <Layout>
      <Layout.TopBar />
      <Layout.Content>
        <S.Container>
          <SectionTitle title="룰렛 현황" description="미니게임 결과에 따라 확률이 조정됩니다" />
          {VIEW_COMPONENTS[currentView]}
          <S.IconButtonWrapper>
            <RouletteViewToggle currentView={currentView} onViewChange={handleViewChange} />
          </S.IconButtonWrapper>
        </S.Container>
      </Layout.Content>
      <Layout.ButtonBar>
        {playerType === 'HOST' ? (
          <Button variant="primary" onClick={handleSpinClick}>
            룰렛 돌리기
          </Button>
        ) : (
          <Button variant="loading" loadingText="대기 중" />
        )}
      </Layout.ButtonBar>
    </Layout>
  );
};

export default RoulettePlayPage;
