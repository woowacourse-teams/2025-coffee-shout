import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import * as S from './PlayerMenu.styled';

const PlayerMenu = () => {
  const { participants } = useParticipants();

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
