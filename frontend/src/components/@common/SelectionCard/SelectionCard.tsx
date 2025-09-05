import * as S from './SelectionCard.styled';

type Props = {
  color: string;
  text: string;
  imgUrl?: string;
};

const SelectionCard = ({ color = '#ffb2b2', text, imgUrl }: Props) => {
  return (
    <S.Container color={color}>
      {imgUrl && <S.Icon src={imgUrl} alt="icon" />}
      <S.Text>{text}</S.Text>
    </S.Container>
  );
};

export default SelectionCard;
