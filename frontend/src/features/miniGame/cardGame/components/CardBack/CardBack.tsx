import CoffeeIcon from '@/assets/images/coffee-white.svg';
import { ComponentProps } from 'react';
import * as S from './CardBack.styled';

type Props = {
  size?: 'small' | 'medium' | 'large';
  onClick: () => void;
} & Omit<ComponentProps<'button'>, 'onClick'>;

const CardBack = ({ size, onClick, ...rest }: Props) => {
  return (
    <S.Container $size={size} onClick={onClick} {...rest}>
      <S.Circle $size={size}>
        <S.CoffeeIcon src={CoffeeIcon} alt="coffee-icon" />
      </S.Circle>
    </S.Container>
  );
};

export default CardBack;
