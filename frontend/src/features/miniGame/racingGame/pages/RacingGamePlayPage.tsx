import RacingPlayer from '../components/RacingPlayer/RacingPlayer';
import RacingLine from '../components/RacingLine/RacingLine';
import * as S from './RacingGamePlayPage.styled';
import Finish from '../components/Finish/Finish';
import Goal from '../components/Goal/Goal';
import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import RacingRank from '../components/RacingRank/RacingRank';
import RacingProgressBar from '../components/RacingProgressBar/RacingProgressBar';
import { useGoalDisplay } from '../hooks/useGoalDisplay';
import { useBackgroundAnimation } from '../hooks/useBackgroundAnimation';
import { usePlayerData } from '../hooks/usePlayerData';
import { getVisiblePlayers } from '../utils/getVisiblePlayers';
import RacingGameOverlay from '../components/RacingGameOverlay/RacingGameOverlay';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import PrepareOverlay from '../../components/PrepareOverlay/PrepareOverlay';
import { useRacingGame } from '@/contexts/RacingGame/RacingGameContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { colorList } from '@/constants/color';

const RacingGamePage = () => {
  const { joinCode, myName } = useIdentifier();
  const { send } = useWebSocket();
  const navigate = useNavigate();
  const { racingGameState, racingGameData } = useRacingGame();
  const { getParticipantColorIndex } = useParticipants();

  const containerRef = useRef<HTMLDivElement | null>(null);

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

  useEffect(() => {
    setTimeout(() => {
      send(`/room/${joinCode}/racing-game/start`, {
        hostName: myName,
      });
    }, 2000);
  }, [joinCode, send, myName]);

  useEffect(() => {
    if (racingGameState === 'DONE') {
      navigate(`/room/${joinCode}/RACING_GAME/result`);
    }
  }, [racingGameState, joinCode, navigate]);

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
              {visiblePlayers.map((player) => (
                <RacingPlayer
                  key={player.playerName}
                  playerName={player.playerName}
                  position={player.position}
                  speed={player.speed}
                  isMe={player.playerName === myName}
                  myPosition={myPosition}
                  color={colorList[getParticipantColorIndex(player.playerName)]}
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
