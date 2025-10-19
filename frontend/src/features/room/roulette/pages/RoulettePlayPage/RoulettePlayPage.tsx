import useFetch from '@/apis/rest/useFetch';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import Button from '@/components/@common/Button/Button';
import LocalErrorBoundary from '@/components/@common/ErrorBoundary/LocalErrorBoundary';
import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import RouletteViewToggle from '@/components/@composition/RouletteViewToggle/RouletteViewToggle';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import Layout from '@/layouts/Layout';
import { MiniGameType } from '@/types/miniGame/common';
import { RouletteView, RouletteWinnerResponse } from '@/types/roulette';
import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import RoulettePlaySection from '../../components/RoulettePlaySection/RoulettePlaySection';
import * as S from './RoulettePlayPage.styled';
import useRoulettePlay from './hooks/useRoulettePlay';

const RoulettePlayPage = () => {
  const { joinCode, myName } = useIdentifier();
  const { playerType } = usePlayerType();
  const { send } = useWebSocket();
  const navigate = useNavigate();
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');
  const { winner, randomAngle, isSpinStarted, handleSpinClick, startSpinWithResult } =
    useRoulettePlay();
  const { probabilityHistory } = useProbabilityHistory();
  const { data: selectedMiniGames } = useFetch<MiniGameType[]>({
    endpoint: `/rooms/minigames/selected?joinCode=${joinCode}`,
    enabled: !!joinCode,
  });

  const handleWinnerData = useCallback(
    (data: RouletteWinnerResponse) => {
      setCurrentView('roulette');
      startSpinWithResult(data);
    },
    [startSpinWithResult]
  );

  // TODO: send를 보내도 여기서 응답이 안오고 있음
  const handleGameStart = useCallback(
    (data: { miniGameType: MiniGameType }) => {
      const { miniGameType: nextMiniGame } = data;
      console.log('nextMiniGame: ', nextMiniGame);
      navigate(`/room/${joinCode}/${nextMiniGame}/ready`);
    },
    [joinCode, navigate]
  );

  useWebSocketSubscription<RouletteWinnerResponse>(`/room/${joinCode}/winner`, handleWinnerData);
  useWebSocketSubscription(`/room/${joinCode}/round`, handleGameStart);

  const handleViewChange = () => {
    setCurrentView((prev) => (prev === 'statistics' ? 'roulette' : 'statistics'));
  };

  // TODO: 다음 미니게임이 있는지 없는지로 구분해야 함
  const hasNextMiniGame = selectedMiniGames && selectedMiniGames.length > 0;

  const handleUnifiedButtonClick = () => {
    if (isSpinStarted) return;

    if (hasNextMiniGame) {
      send(`/room/${joinCode}/minigame/command`, {
        commandType: 'START_MINI_GAME',
        commandRequest: {
          hostName: myName,
        },
      });
    } else {
      handleSpinClick();
    }
  };

  const getButtonText = () => {
    if (isSpinStarted) return '룰렛 돌리는 중';
    if (hasNextMiniGame) return '다음 미니게임 하러가기';
    return '룰렛 돌리기';
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
          <Button
            variant={isSpinStarted ? 'disabled' : 'primary'}
            onClick={handleUnifiedButtonClick}
          >
            {getButtonText()}
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
