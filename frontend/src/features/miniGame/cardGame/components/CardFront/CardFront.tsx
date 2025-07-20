import { ComponentProps } from 'react';
import * as S from './CardFront.styled';
import { Card } from '../../constants/cards';

// TODO: 색상 추가 필요, PlayerCard 부분과 합칠 것
export type IconColor = 'red';

type Player = {
  name: string;
  iconColor: IconColor;
};

type Props = {
  size?: 'small' | 'medium' | 'large';
  onClick: () => void;
  player?: Player;
  card: Card;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const getCardImageSrc = (card: Card) => {
  const { type, value } = card;
  const prefix = value < 0 ? 'minus' : value > 0 ? 'plus' : '';
  const absValue = Math.abs(value);

  return `/images/cards/${type}-${prefix}${absValue}.svg`;
};

const getPlayerIconSrc = (iconColor: IconColor) => {
  return `/images/profile-${iconColor}.svg`;
};

const CardFront = ({ size, onClick, player, card, ...rest }: Props) => {
  return (
    <S.Container $size={size} onClick={onClick} {...rest}>
      <S.Circle $size={size}>
        <S.CardImage
          src={getCardImageSrc(card)}
          alt={`${card.type} ${card.value > 0 && '+'}${card.value}`}
        />
      </S.Circle>
      {player && (
        <S.Player $size={size}>
          <S.PlayerIcon
            src={getPlayerIconSrc(player.iconColor)}
            alt={`player-${player.name}-icon`}
            $size={size}
          />
          <S.PlayerName $size={size}>{player.name}</S.PlayerName>
        </S.Player>
      )}
    </S.Container>
  );
};

export default CardFront;
