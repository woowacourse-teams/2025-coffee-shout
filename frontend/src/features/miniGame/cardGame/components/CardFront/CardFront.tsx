import { ComponentProps } from 'react';
import * as S from './CardFront.styled';

type Player = {
  name: string;
  iconSrc: string;
};

type Props = {
  size?: 'small' | 'medium' | 'large';
  onClick: () => void;
  player?: Player;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const CardFront = ({ size, onClick, player, ...rest }: Props) => {
  return (
    <S.Container $size={size} onClick={onClick} {...rest}>
      <S.Circle $size={size}>{/* 카드 숫자 정보 */}</S.Circle>
      {player && player.name && player.iconSrc && (
        <S.Player $size={size}>
          <S.PlayerIcon src={player.iconSrc} alt={`player-${player.name}-icon`} $size={size} />
          <S.PlayerName $size={size}>{player.name}</S.PlayerName>
        </S.Player>
      )}
    </S.Container>
  );
};

export default CardFront;
