import Button from '@/components/@common/Button/Button';
import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import RoulettePlaySection from '../../components/RoulettePlaySection/RoulettePlaySection';
import * as S from './RoulettePlayPage.styled';
import useRoulettePlay from './hooks/useRoulettePlay';
import { RouletteView, RouletteWinnerResponse } from '@/types/roulette';
import { useCallback, useState } from 'react';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import RouletteViewToggle from '@/components/@composition/RouletteViewToggle/RouletteViewToggle';
import LocalErrorBoundary from '@/components/@common/ErrorBoundary/LocalErrorBoundary';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';

const RoulettePlayPage = () => {
  const { joinCode } = useIdentifier();
  const { playerType } = usePlayerType();
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');
  const { winner, randomAngle, isSpinStarted, handleSpinClick, startSpinWithResult } =
    useRoulettePlay();
  const { probabilityHistory } = useProbabilityHistory();

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
        isSpinStarted={isSpinStarted}
        winner={winner}
        randomAngle={randomAngle}
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
          <LocalErrorBoundary>{VIEW_COMPONENTS[currentView]}</LocalErrorBoundary>
          <S.IconButtonWrapper>
            <RouletteViewToggle currentView={currentView} onViewChange={handleViewChange} />
          </S.IconButtonWrapper>
        </S.Container>
      </Layout.Content>
      <Layout.ButtonBar>
        {playerType === 'HOST' ? (
          <Button variant={isSpinStarted ? 'disabled' : 'primary'} onClick={handleSpinClick}>
            {isSpinStarted ? '룰렛 돌리는 중' : '룰렛 돌리기'}
          </Button>
        ) : (
          <Button variant={isSpinStarted ? 'disabled' : 'loading'} loadingText="대기 중">
            룰렛 돌리는 중
          </Button>
        )}
      </Layout.ButtonBar>
    </Layout>
  );
};

export default RoulettePlayPage;
