import Description from '@/components/@common/Description/Description';
import * as S from './RankItem.styled';

type Props = {
  playerName: string;
  rank: number;
  isMe: boolean;
};

const RankItem = ({ playerName, rank, isMe }: Props) => {
  return (
    <S.Container>
      <S.RankNumber>
        <Description color={isMe ? 'point-500' : 'white'}>{rank}</Description>
      </S.RankNumber>
      <Description color={isMe ? 'point-500' : 'white'}>{playerName}</Description>
    </S.Container>
  );
};

export default RankItem;
