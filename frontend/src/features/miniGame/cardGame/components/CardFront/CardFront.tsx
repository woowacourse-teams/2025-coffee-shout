import { ComponentProps } from 'react';
import * as S from './CardFront.styled';

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
} & Omit<ComponentProps<'button'>, 'onClick'>;

const CardFront = ({ size, onClick, player, ...rest }: Props) => {
  return (
    <S.Container $size={size} onClick={onClick} {...rest}>
      <S.Circle $size={size}>{/* 카드 숫자 정보 */}</S.Circle>
      {player && (
        <S.Player $size={size}>
          <S.PlayerIcon
            src={`/images/profile-${player.iconColor}.svg`}
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
