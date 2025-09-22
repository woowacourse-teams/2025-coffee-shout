import Headline3 from '@/components/@common/Headline3/Headline3';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';
import * as S from './MenuCount.styled';
import { TemperatureOption } from '@/types/menu';

const MenuCount = () => {
  const { participants } = useParticipants();
  const simpleViewMap = new Map<string, number>();

  participants.forEach((participant) => {
    const { menuResponse } = participant;
    const menuKey = createMenuKey(menuResponse.name, menuResponse.temperature);
    const count = simpleViewMap.get(menuKey) || 0;
    simpleViewMap.set(menuKey, count + 1);
  });

  return (
    <S.OrderList>
      <S.Divider />
      {[...simpleViewMap].map(([menuKey, count], index) => (
        <S.OrderItem key={index}>
          <Paragraph>{menuKey}</Paragraph>
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

const createMenuKey = (menuName: string, menuTemperature: TemperatureOption) => {
  return `${menuName} (${menuTemperature})`;
};
