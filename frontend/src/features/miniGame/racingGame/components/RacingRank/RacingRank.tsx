import { useMemo } from 'react';
import * as S from './RacingRank.styled';

interface Player {
  playerName: string;
  x: number;
}

interface RacingRankProps {
  players: Player[];
  myName: string;
}

const RacingRank = ({ players, myName }: RacingRankProps) => {
  const sortedPlayers = useMemo(() => {
    return [...players].sort((a, b) => b.x - a.x);
  }, [players]);

  const topThree = useMemo(() => {
    return sortedPlayers.slice(0, 3);
  }, [sortedPlayers]);

  return (
    <S.Container>
      <S.RankList>
        {topThree.map((player, index) => {
          const isMe = player.playerName === myName;
          return (
            <S.RankItem key={player.playerName} $rank={index + 1} $isMe={isMe}>
              <S.RankNumber $rank={index + 1}>{index + 1}</S.RankNumber>
              <S.PlayerName $isMe={isMe}>{player.playerName}</S.PlayerName>
            </S.RankItem>
          );
        })}
      </S.RankList>
    </S.Container>
  );
};

export default RacingRank;
