import Headline4 from '@/components/@common/Headline4/Headline4';
import { IconColor, UserRole } from '@/types/player';
import { PropsWithChildren } from 'react';
import * as S from './PlayerCard.styled';
import { getPlayerIcon } from './utils/getPlayerIcon';
import Crown from '@/assets/crown.svg';

type Props = {
  name: string;
  iconColor: IconColor;
  userRole?: UserRole;
} & PropsWithChildren;

const PlayerCard = ({ name, iconColor, userRole = 'GUEST', children }: Props) => {
  return (
    <S.Container>
      <S.Wrapper>
        <S.PlayerIcon src={getPlayerIcon(iconColor)} alt={`player-${name}-icon`} />
        <S.NameWrapper>
          <Headline4>{name}</Headline4>
          {userRole === 'HOST' && <S.CrownIcon src={Crown} alt="crown" />}
        </S.NameWrapper>
      </S.Wrapper>
      {children}
    </S.Container>
  );
};

export default PlayerCard;
