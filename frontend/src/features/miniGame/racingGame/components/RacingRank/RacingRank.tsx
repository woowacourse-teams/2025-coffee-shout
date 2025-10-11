import { useMemo } from 'react';
import Description from '@/components/@common/Description/Description';
import * as S from './RacingRank.styled';

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
            <S.RankItem key={player.playerName}>
              <S.RankNumber>
                <Description color={isMe ? 'point-500' : 'white'}>{index + 1}</Description>
              </S.RankNumber>

              <Description color={isMe ? 'point-500' : 'white'}>{player.playerName}</Description>
            </S.RankItem>
          );
        })}
      </S.RankList>
    </S.Container>
  );
};

export default RacingRank;
