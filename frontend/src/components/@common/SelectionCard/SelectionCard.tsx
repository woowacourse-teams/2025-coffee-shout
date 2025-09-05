import * as S from './SelectionCard.styled';

type Props = {
  color: string;
  text: string;
  imageUrl?: string;
};

const SelectionCard = ({ color = '#ffb2b2', text, imageUrl }: Props) => {
  return (
    <S.Container color={color}>
      {imageUrl && <S.Icon src={imageUrl} alt="icon" />}
      <S.Text>{text}</S.Text>
    </S.Container>
  );
};

export default SelectionCard;
