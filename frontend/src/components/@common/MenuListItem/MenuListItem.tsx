import * as S from './MenuListItem.styled';

type Props = {
  text: string;
  onClick: () => void;
};

const MenuListItem = ({ text, onClick }: Props) => {
  return (
    <S.Container onClick={onClick}>
      <S.Text>{text}</S.Text>
    </S.Container>
  );
};

export default MenuListItem;
