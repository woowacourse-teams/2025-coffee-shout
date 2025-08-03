import Paragraph from '@/components/@common/Paragraph/Paragraph';
import * as S from './PlayerMenu.styled';
import { Player } from '@/types/player';

type Props = {
  participants: Player[];
};

const PlayerMenu = ({ participants }: Props) => {
  return (
    <S.OrderList>
      {participants.map((participant, index) => (
        <S.OrderItem key={index}>
          <Paragraph>{participant.playerName}</Paragraph>
          <Paragraph>{participant.menuResponse.name}</Paragraph>
        </S.OrderItem>
      ))}
    </S.OrderList>
  );
};

export default PlayerMenu;
