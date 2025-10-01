import RacingPlayer from './components/RacingPlayer/RacingPlayer';
import RacingLine from './components/RacingLine/RacingLine';
import * as S from './RacingGamePage.styled';
import PrepareOverlay from '../cardGame/components/PrepareOverlay/PrepareOverlay';
import { useRacingGameMock } from '@/features/miniGame/racingGame/mock/useRacingGameMock';
import Finish from './components/Finish/Finish';
import Goal from './components/Goal/Goal';
import { useRef } from 'react';
import RacingRank from './components/RacingRank/RacingRank';
import RacingProgressBar from './components/RacingProgressBar/RacingProgressBar';
import { useGoalDisplay } from './hooks/useGoalDisplay';
import { useBackgroundAnimation } from './hooks/useBackgroundAnimation';
import { usePlayerData } from './hooks/usePlayerData';
import { getVisiblePlayers } from './utils/getVisiblePlayers';
// import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';

const myName = '김철수';

const RacingGamePage = () => {
  const { racingGameState, racingGameData } = useRacingGameMock();
  const containerRef = useRef<HTMLDivElement | null>(null);

  const visiblePlayers = getVisiblePlayers(racingGameData.players, myName);

  const { myX, mySpeed } = usePlayerData({
    players: visiblePlayers,
    myName,
  });

  const showGoal = useGoalDisplay({
    myX,
    endDistance: racingGameData.distance.end,
    gameState: racingGameState,
  });

  useBackgroundAnimation({
    containerRef,
    mySpeed,
  });

  // const handleRacingGameData = useCallback((data: RacingGameState) => {
  //   setRacingGameData(data);
  // }, []);

  // const handleRacingGameState = useCallback((data: RacingGameState) => {
  //   setRacingGameState(data);
  // }, []);

  // useWebSocketSubscription('/room/${joinCode}/racingGame', handleRacingGameData);
  // useWebSocketSubscription('/room/${joinCode}/racingGame/state', handleRacingGameState);

  return (
    <>
      {racingGameState === 'READY' && <PrepareOverlay />}
      {racingGameState === 'FINISH' && <Finish />}
      {showGoal && <Goal />}
      <S.Container ref={containerRef}>
        <RacingRank players={racingGameData.players} myName={myName} />
        <RacingProgressBar
          myName={myName}
          endDistance={racingGameData.distance.end}
          players={racingGameData.players}
        />
        <S.ContentWrapper>
          <S.PlayersWrapper>
            {/* 출발선 */}
            <RacingLine x={0} myX={myX} />
            {/* 도착선 */}
            <RacingLine x={1000} myX={myX} />
            {visiblePlayers.map((player, index) => (
              <RacingPlayer
                key={player.playerName}
                playerName={player.playerName}
                x={player.x}
                speed={player.speed}
                isMe={player.playerName === myName}
                myX={myX}
                colorIndex={index}
              />
            ))}
          </S.PlayersWrapper>
        </S.ContentWrapper>
      </S.Container>
    </>
  );
};

export default RacingGamePage;
