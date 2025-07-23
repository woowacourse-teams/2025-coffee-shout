import CardIcon from '@/assets/sign-inversion-icon.svg';
import { Card } from '../../constants/cards';
import * as S from './CardFront.styled';

// TODO: 색상 추가 필요, PlayerCard 부분과 합칠 것
export type IconColor = 'red';

type Player = {
  name: string;
  iconColor: IconColor;
};

type Props = {
  size?: 'small' | 'medium' | 'large';
  player?: Player;
  card: Card;
};

const CardFront = ({ size, player, card }: Props) => {
  const isSignInversionCard = card.type === 'MULTIPLIER' && card.value === -1;

  return (
    <S.Container $size={size}>
      <S.Circle $size={size}>
        {isSignInversionCard ? (
          <S.CardIcon src={CardIcon} alt="부호 반전" />
        ) : (
          <S.CardText $size={size} $card={card}>
            {getCardText(card)}
          </S.CardText>
        )}
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

const getCardText = (card: Card) => {
  const { type, value } = card;
  if (type === 'ADDITION') return value >= 0 ? `+${value}` : `${value}`;
  else return value === -1 ? null : `x${value}`;
};

const getPlayerIconSrc = (iconColor: IconColor) => {
  return `@/assets/profile-${iconColor}.svg`;
};
