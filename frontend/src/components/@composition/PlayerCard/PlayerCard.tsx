import Crown from '@/assets/crown.svg';
import Headline4 from '@/components/@common/Headline4/Headline4';
import { ColorList } from '@/constants/color';
import { PlayerType } from '@/types/player';
import { PropsWithChildren } from 'react';
import * as S from './PlayerCard.styled';
import PlayerIcon from '@/components/@common/PlayerIcon/PlayerIcon';

type Props = {
  name: string;
  playerColor: ColorList;
  playerType?: PlayerType;
} & PropsWithChildren;

const PlayerCard = ({ name, playerColor, playerType = 'GUEST', children }: Props) => {
  return (
    <S.Container data-testid="player-card">
      <S.Wrapper>
        <PlayerIcon color={playerColor} />
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
