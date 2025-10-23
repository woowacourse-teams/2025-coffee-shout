import * as S from './SelectionCard.styled';

type Props = {
  color: string;
  text: string;
  imageUrl?: string;
  ariaLabel?: string;
};

const SelectionCard = ({ color = '#ffb2b2', text, imageUrl, ariaLabel }: Props) => {
  return (
    <S.Container color={color} aria-label={ariaLabel}>
      {imageUrl && <S.Icon src={imageUrl} alt="icon" aria-hidden="true" />}
      <S.Text aria-hidden="true">{text}</S.Text>
    </S.Container>
  );
};

export default SelectionCard;
