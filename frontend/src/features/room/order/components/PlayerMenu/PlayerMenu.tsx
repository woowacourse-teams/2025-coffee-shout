import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { ParticipantResponse } from '../../../lobby/pages/LobbyPage';
import * as S from './PlayerMenu.styled';

type Props = {
  participants: ParticipantResponse;
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
