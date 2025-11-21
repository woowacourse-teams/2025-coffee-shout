import { useMemo, useRef } from 'react';
import { useRacingGameData } from '@/contexts/RacingGame/RacingGameContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import RankItem from '../RankItem/RankItem';
import * as S from './RacingRanks.styled';

type Player = {
  playerName: string;
  position: number;
};

const RacingRanks = () => {
  const racingGameData = useRacingGameData();
  const { myName } = useIdentifier();
  const finishOrderRef = useRef<Player[]>([]);

  const rankedPlayers = useMemo(() => {
    const finishOrder = finishOrderRef.current;
    const { players, distance } = racingGameData;

    players.forEach(({ playerName, position }) => {
      if (
        position >= distance.end &&
        !finishOrder.some((player) => player.playerName === playerName)
      ) {
        finishOrder.push({ playerName, position });
      }
    });

    const unFinishedSortedPlayers = players
      .filter((player) => !finishOrder.some((p) => p.playerName === player.playerName))
      .sort((a, b) => b.position - a.position);

    return [...finishOrder, ...unFinishedSortedPlayers];
  }, [racingGameData.players, racingGameData.distance.end]);

  return (
    <S.Container>
      <S.RankList>
        {rankedPlayers.map((player, index) => {
          const isMe = player.playerName === myName;

          return (
            <RankItem
              key={player.playerName}
              playerName={player.playerName}
              rank={index + 1}
              isMe={isMe}
              isFixed={player.position >= racingGameData.distance.end}
            />
          );
        })}
      </S.RankList>
    </S.Container>
  );
};

export default RacingRanks;
