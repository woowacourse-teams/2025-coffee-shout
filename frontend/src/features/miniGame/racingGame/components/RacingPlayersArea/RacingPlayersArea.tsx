import { useRacingGame } from '@/contexts/RacingGame/RacingGameContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import { useMemo, useRef } from 'react';
import { colorList } from '@/constants/color';
import RacingLine from '../RacingLine/RacingLine';
import RacingPlayer from '../RacingPlayer/RacingPlayer';
import Goal from '../Goal/Goal';
import { useBackgroundAnimation } from '../../hooks/useBackgroundAnimation';
import { useGoalDisplay } from '../../hooks/useGoalDisplay';
import { usePlayerData } from '../../hooks/usePlayerData';
import { getVisiblePlayers } from '../../utils/getVisiblePlayers';
import * as S from './RacingPlayersArea.styled';

const FINISH_LINE_VISUAL_OFFSET = 30;

const RacingPlayersArea = () => {
  const { racingGameData, racingGameState } = useRacingGame();
  const { myName } = useIdentifier();
  const { getParticipantColorIndex } = useParticipants();

  const containerRef = useRef<HTMLDivElement | null>(null);

  const visiblePlayers = useMemo(
    () => getVisiblePlayers(racingGameData.players, myName),
    [
      racingGameData.players.map((p) => `${p.playerName}:${p.position}`).join(','),
      myName,
    ]
  );

  const { myPosition, mySpeed } = usePlayerData({
    players: visiblePlayers,
    myName,
  });

  const isGoal = useGoalDisplay({
    myPosition,
    endDistance: racingGameData.distance.end,
  });

  useBackgroundAnimation({
    containerRef,
    mySpeed,
  });

  return (
    <S.Container ref={containerRef}>
      <S.ContentWrapper>
        <S.PlayersWrapper>
          <RacingLine position={racingGameData.distance.start} myPosition={myPosition} />
          <RacingLine
            position={racingGameData.distance.end - FINISH_LINE_VISUAL_OFFSET}
            myPosition={myPosition}
          />
          {visiblePlayers.map((player) => (
            <RacingPlayer
              key={player.playerName}
              player={player}
              isMe={player.playerName === myName}
              myPosition={myPosition}
              color={colorList[getParticipantColorIndex(player.playerName)]}
            />
          ))}
        </S.PlayersWrapper>
      </S.ContentWrapper>
      {isGoal && racingGameState === 'PLAYING' && <Goal />}
    </S.Container>
  );
};

export default RacingPlayersArea;

