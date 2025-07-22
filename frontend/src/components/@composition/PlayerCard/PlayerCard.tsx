import Headline4 from '@/components/@common/Headline4/Headline4';
import { PropsWithChildren } from 'react';
import * as S from './PlayerCard.styled';

// TODO: 색상 추가 필요
type IconColor = 'red';

type Props = {
  name: string;
  iconColor: IconColor;
} & PropsWithChildren;

const PlayerCard = ({ name, iconColor, children }: Props) => {
  return (
    <S.Container>
      <S.Wrapper>
        <S.PlayerIcon src={`./images/profile-${iconColor}.svg`} alt={`player-${name}-icon`} />
        <S.NameWrapper>
          <Headline4>{name}</Headline4>
        </S.NameWrapper>
      </S.Wrapper>
      {children}
    </S.Container>
  );
};

export default PlayerCard;
