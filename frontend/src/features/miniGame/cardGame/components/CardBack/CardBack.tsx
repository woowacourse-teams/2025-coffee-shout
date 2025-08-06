import CoffeeIcon from '@/assets/coffee-white.svg';
import { Size } from '@/types/styles';
import { ComponentProps } from 'react';
import * as S from './CardBack.styled';

type Props = {
  size?: Size;
} & ComponentProps<'button'>;

const CardBack = ({ size, disabled, ...rest }: Props) => {
  return (
    <S.Container $size={size} $disabled={disabled} disabled={disabled} {...rest}>
      <S.Circle $size={size}>
        <S.CoffeeIcon src={CoffeeIcon} alt="coffee-icon" />
      </S.Circle>
    </S.Container>
  );
};

export default CardBack;
