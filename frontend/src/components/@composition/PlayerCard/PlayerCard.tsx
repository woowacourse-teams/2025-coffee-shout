import Crown from '@/assets/crown.svg';
import Headline4 from '@/components/@common/Headline4/Headline4';
import { ColorList } from '@/constants/color';
import { PlayerType } from '@/types/player';
import { PropsWithChildren } from 'react';
import * as S from './PlayerCard.styled';
import { getPlayerIcon } from './utils/getPlayerIcon';

type Props = {
  name: string;
  iconColor: ColorList;
  playerType?: PlayerType;
} & PropsWithChildren;

const PlayerCard = ({ name, iconColor, playerType = 'GUEST', children }: Props) => {
  return (
    <S.Container>
      <S.Wrapper>
        <S.PlayerIcon src={getPlayerIcon(iconColor)} alt={`player-${name}-icon`} />
        <S.NameWrapper>
          <Headline4>{name}</Headline4>
          {playerType === 'HOST' && <S.CrownIcon src={Crown} alt="crown" />}
        </S.NameWrapper>
      </S.Wrapper>
      {children}
    </S.Container>
  );
};

export default PlayerCard;
