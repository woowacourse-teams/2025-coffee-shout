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
        <S.CoffeeIcon src={'./images/coffee-white.svg'} alt="coffee-icon" />
      </S.Circle>
    </S.Container>
  );
};

export default CardBack;
