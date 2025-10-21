import { useMemo } from 'react';
import RankItem from '../RankItem/RankItem';
import * as S from './RacingRanks.styled';

type Player = {
  playerName: string;
  position: number;
};

type Props = {
  players: Player[];
  myName: string;
};

const RacingRank = ({ players, myName }: Props) => {
  const sortedPlayers = useMemo(() => {
    return [...players].sort((a, b) => b.position - a.position);
  }, [players]);

  return (
    <S.Container>
      <S.RankList>
        {sortedPlayers.map((player, index) => {
          const isMe = player.playerName === myName;
          return (
            <RankItem
              key={player.playerName}
              playerName={player.playerName}
              rank={index + 1}
              isMe={isMe}
            />
          );
        })}
      </S.RankList>
    </S.Container>
  );
};

export default RacingRank;
