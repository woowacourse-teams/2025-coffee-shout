import Headline3 from '@/components/@common/Headline3/Headline3';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import * as S from './MenuCount.styled';

const MenuCount = () => {
  const { participants } = useParticipants();
  const simpleViewMap = new Map<string, number>();

  participants.forEach((participant) => {
    const { menuResponse } = participant;
    const count = simpleViewMap.get(menuResponse.name) || 0;
    simpleViewMap.set(menuResponse.name, count + 1);
  });

  return (
    <S.OrderList>
      <S.Divider />
      {[...simpleViewMap].map(([menuName, count], index) => (
        <S.OrderItem key={index}>
          <Paragraph>{menuName}</Paragraph>
          <Paragraph>{count}개</Paragraph>
        </S.OrderItem>
      ))}
      <S.Divider />
      <S.TotalWrapper>
        <Headline3>총 {participants.length}개</Headline3>
      </S.TotalWrapper>
    </S.OrderList>
  );
};

export default MenuCount;
