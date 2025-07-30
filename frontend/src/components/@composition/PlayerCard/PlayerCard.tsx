import Headline4 from '@/components/@common/Headline4/Headline4';
import { IconColor, PlayerType } from '@/types/player';
import { PropsWithChildren } from 'react';
import * as S from './PlayerCard.styled';
import { getPlayerIcon } from './utils/getPlayerIcon';
import Crown from '@/assets/crown.svg';

type Props = {
  name: string;
  iconColor: IconColor;
  playerRole?: PlayerType;
} & PropsWithChildren;

const PlayerCard = ({ name, iconColor, playerRole = 'GUEST', children }: Props) => {
  return (
    <S.Container>
      <S.Wrapper>
        <S.PlayerIcon src={getPlayerIcon(iconColor)} alt={`player-${name}-icon`} />
        <S.NameWrapper>
          <Headline4>{name}</Headline4>
          {playerRole === 'HOST' && <S.CrownIcon src={Crown} alt="crown" />}
        </S.NameWrapper>
      </S.Wrapper>
      {children}
    </S.Container>
  );
};

export default PlayerCard;
