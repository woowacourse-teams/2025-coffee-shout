import Headline4 from '@/components/@common/Headline4/Headline4';
import { PropsWithChildren } from 'react';
import * as S from './PlayerCard.styled';

type Props = {
  name: string;
  iconSrc: string;
} & PropsWithChildren;

const PlayerCard = ({ name, iconSrc, children }: Props) => {
  return (
    <S.Container>
      <S.Wrapper>
        <S.PlayerIcon src={iconSrc} alt={`player-${name}-icon`} />
        <Headline4>{name}</Headline4>
      </S.Wrapper>
      {children}
    </S.Container>
  );
};

export default PlayerCard;
