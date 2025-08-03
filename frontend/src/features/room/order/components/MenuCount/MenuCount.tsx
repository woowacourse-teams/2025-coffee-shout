import Headline3 from '@/components/@common/Headline3/Headline3';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { ParticipantResponse } from '../../../lobby/pages/LobbyPage';
import * as S from './MenuCount.styled';

type Props = {
  participants: ParticipantResponse;
};

const MenuCount = ({ participants }: Props) => {
  const simpleViewMap = new Map();

  participants.forEach((participant) => {
    const { menuResponse } = participant;
    const count = simpleViewMap.get(menuResponse.name) || 0;
    simpleViewMap.set(menuResponse.name, count + 1);
  });

  return (
    <S.OrderList>
      <S.Divider />
      {[...simpleViewMap].map((item, index) => (
        <S.OrderItem key={index}>
          <Paragraph>{item[0]}</Paragraph>
          <Paragraph>{item[1]}개</Paragraph>
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
