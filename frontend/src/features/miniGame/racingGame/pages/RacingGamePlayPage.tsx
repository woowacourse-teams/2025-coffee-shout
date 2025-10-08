import RacingPlayer from '../components/RacingPlayer/RacingPlayer';
import RacingLine from '../components/RacingLine/RacingLine';
import * as S from './RacingGamePlayPage.styled';
import Finish from '../components/Finish/Finish';
import Goal from '../components/Goal/Goal';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import RacingRank from '../components/RacingRank/RacingRank';
import RacingProgressBar from '../components/RacingProgressBar/RacingProgressBar';
import { useGoalDisplay } from '../hooks/useGoalDisplay';
import { useBackgroundAnimation } from '../hooks/useBackgroundAnimation';
import { usePlayerData } from '../hooks/usePlayerData';
import { getVisiblePlayers } from '../utils/getVisiblePlayers';
import RacingGameOverlay from '../components/RacingGameOverlay/RacingGameOverlay';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import PrepareOverlay from '../../components/PrepareOverlay/PrepareOverlay';

import { useRacingGame } from '@/contexts/RacingGame/RacingGameContext';

type RacingGameData = {
  distance: {
    start: number;
    end: number;
  };
  players: Array<{
    playerName: string;
    position: number; // 서버에서 position으로 보내고 있음
    speed: number;
  }>;
};

const RacingGamePage = () => {
  const { joinCode, myName } = useIdentifier();
  const { send } = useWebSocket();
  const navigate = useNavigate();
  const { racingGameState } = useRacingGame();

  const [racingGameData, setRacingGameData] = useState<RacingGameData>({
    players: [],
    distance: {
      start: 0,
      end: 1000,
    },
  });
  const containerRef = useRef<HTMLDivElement | null>(null);
  const { miniGameType } = useParams();

  const visiblePlayers = getVisiblePlayers(racingGameData.players, myName);

  const { myPosition, mySpeed } = usePlayerData({
    players: visiblePlayers,
    myName,
  });

  const showGoal = useGoalDisplay({
    myPosition,
    endDistance: racingGameData.distance.end,
    gameState: racingGameState,
  });

  useBackgroundAnimation({
    containerRef,
    mySpeed,
  });

  const handleRacingGameData = useCallback((data: RacingGameData) => {
    setRacingGameData(data);
  }, []);

  useWebSocketSubscription(`/room/${joinCode}/racing-game`, handleRacingGameData);

  useEffect(() => {
    setTimeout(() => {
      send(`/room/${joinCode}/racing-game/start`, {
        hostName: myName,
      });
    }, 2000);
  }, [joinCode, send, myName]);

  useEffect(() => {
    if (racingGameState === 'DONE') {
      navigate(`/room/${joinCode}/${miniGameType}/result`);
    }
  }, [racingGameState, joinCode, navigate, miniGameType]);

  return (
    <>
      {racingGameState === 'PREPARE' && <PrepareOverlay />}
      {racingGameState === 'DONE' && <Finish />}
      {showGoal && <Goal />}
      <RacingGameOverlay>
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
              <RacingLine position={0} myPosition={myPosition} />
              {/* 도착선 */}
              <RacingLine position={1000} myPosition={myPosition} />
              {visiblePlayers.map((player, index) => (
                <RacingPlayer
                  key={player.playerName}
                  playerName={player.playerName}
                  position={player.position}
                  speed={player.speed}
                  isMe={player.playerName === myName}
                  myPosition={myPosition}
                  colorIndex={index}
                />
              ))}
            </S.PlayersWrapper>
          </S.ContentWrapper>
        </S.Container>
      </RacingGameOverlay>
    </>
  );
};

export default RacingGamePage;
