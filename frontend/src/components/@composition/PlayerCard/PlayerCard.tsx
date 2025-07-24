import Headline4 from '@/components/@common/Headline4/Headline4';
import { IconColor } from '@/types/player';
import { PropsWithChildren } from 'react';
import * as S from './PlayerCard.styled';
import { getPlayerIcon } from './utils/getPlayerIcon';

type Props = {
  name: string;
  iconColor: IconColor;
} & PropsWithChildren;

const PlayerCard = ({ name, iconColor, children }: Props) => {
  return (
    <S.Container>
      <S.Wrapper>
        <S.PlayerIcon src={getPlayerIcon(iconColor)} alt={`player-${name}-icon`} />
        <S.NameWrapper>
          <Headline4>{name}</Headline4>
        </S.NameWrapper>
      </S.Wrapper>
      {children}
    </S.Container>
  );
};

export default PlayerCard;
