import * as S from './SelectionCard.styled';

type Props = {
  color: string;
  text: string;
  iconSrc?: string;
};

const SelectionCard = ({ color = '#ffb2b2', text, iconSrc }: Props) => {
  return (
    <S.Container color={color}>
      <S.IconContainer>{iconSrc && <S.Icon src={iconSrc} alt="icon" />}</S.IconContainer>
      <S.Text>{text}</S.Text>
    </S.Container>
  );
};

export default SelectionCard;
