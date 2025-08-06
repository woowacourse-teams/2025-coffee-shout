import CardIcon from '@/assets/sign-inversion-icon.svg';
import { ColorList } from '@/constants/color';
import { Size } from '@/types/styles';
import { ComponentProps } from 'react';
import { Card } from '../../constants/cards';
import * as S from './CardFront.styled';

type Props = {
  size?: Size;
  playerIconColor?: ColorList;
  card: Card;
} & ComponentProps<'div'>;

const CardFront = ({ size, playerIconColor, card, ...rest }: Props) => {
  const isSignInversionCard = card.type === 'MULTIPLIER' && card.value === -1;

  return (
    <S.Container $size={size} $playerIconColor={playerIconColor} {...rest}>
      <S.Circle $size={size}>
        {isSignInversionCard ? (
          <S.CardIcon src={CardIcon} alt="부호 반전" />
        ) : (
          <S.CardText $size={size} $card={card}>
            {getCardText(card)}
          </S.CardText>
        )}
      </S.Circle>
    </S.Container>
  );
};

export default CardFront;

const getCardText = (card: Card) => {
  const { type, value } = card;
  if (type === 'ADDITION') return value >= 0 ? `+${value}` : `${value}`;
  else return value === -1 ? null : `x${value}`;
};
