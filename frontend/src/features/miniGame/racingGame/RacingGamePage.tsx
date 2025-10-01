import Headline4 from '@/components/@common/Headline4/Headline4';
import RacingPlayer from './components/RacingPlayer/RacingPlayer';
import * as S from './RacingGamePage.styled';
import PrepareOverlay from '../cardGame/components/PrepareOverlay/PrepareOverlay';
import { useRacingGameMock } from '@/features/miniGame/racingGame/mock/useRacingGameMock';
import Finish from './components/Finish/Finish';
import { useEffect, useRef } from 'react';
// import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';

const myName = '정민수';

const RacingGamePage = () => {
  const { racingGameState, racingGameData } = useRacingGameMock();
  const containerRef = useRef<HTMLDivElement>(null);
  const backgroundPositionRef = useRef(0);

  const myPlayer = racingGameData.players.find((player) => player.playerName === myName);
  const myX = myPlayer?.x ?? 0;
  const mySpeed = myPlayer?.speed ?? 0;

  // 배경 애니메이션
  useEffect(() => {
    let frameId: number;
    let lastTime = performance.now();

    const update = (time: number) => {
      const delta = (time - lastTime) / 1000; // 초 단위
      lastTime = time;

      backgroundPositionRef.current += mySpeed * delta * 2.5; // 속도에 비례해 증가
      if (containerRef.current) {
        containerRef.current.style.backgroundPosition = `${backgroundPositionRef.current}% center`;
      }

      frameId = requestAnimationFrame(update);
    };

    frameId = requestAnimationFrame(update);
    return () => cancelAnimationFrame(frameId);
  }, [mySpeed]);

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
      <S.Container ref={containerRef}>
        <S.HeadlineWrapper>
          <Headline4>레이싱 게임</Headline4>
        </S.HeadlineWrapper>
        <S.ContentWrapper>
          <S.PlayersWrapper>
            {racingGameData.players.map((player, index) => (
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
