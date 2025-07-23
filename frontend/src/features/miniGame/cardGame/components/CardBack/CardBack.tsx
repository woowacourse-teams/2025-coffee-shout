import CoffeeIcon from '@/assets/coffee-white.svg';
import { ComponentProps } from 'react';
import * as S from './CardBack.styled';

type Props = {
  size?: 'small' | 'medium' | 'large';
} & ComponentProps<'button'>;

const CardBack = ({ size, disabled, onClick, ...rest }: Props) => {
  return (
    <S.Container $size={size} $disabled={disabled} onClick={onClick} {...rest}>
      <S.Circle $size={size}>
        <S.CoffeeIcon src={CoffeeIcon} alt="coffee-icon" />
      </S.Circle>
    </S.Container>
  );
};

export default CardBack;
