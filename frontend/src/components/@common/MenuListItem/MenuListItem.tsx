import * as S from './MenuListItem.styled';

type Props = {
  text: string;
};

const MenuListItem = ({ text }: Props) => {
  return (
    <S.Container>
      <S.Text>{text}</S.Text>
    </S.Container>
  );
};

export default MenuListItem;
