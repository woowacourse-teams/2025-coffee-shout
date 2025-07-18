import * as S from './CardBack.styled';

type Props = {
  size?: 'small' | 'medium' | 'large';
};

const CardBack = ({ size }: Props) => {
  return (
    <S.Container $size={size}>
      <S.Circle $size={size}>
        <S.CoffeeIcon src={'/images/coffee-white.svg'} alt="coffee-icon" />
      </S.Circle>
    </S.Container>
  );
};

export default CardBack;
