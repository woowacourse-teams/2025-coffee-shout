import Paragraph from '../Paragraph/Paragraph';
import * as S from './MenuListItem.styled';

type Props = {
  text: string;
  onClick: () => void;
};

const MenuListItem = ({ text, onClick }: Props) => {
  return (
    <S.Container onClick={onClick}>
      <Paragraph>{text}</Paragraph>
    </S.Container>
  );
};

export default MenuListItem;
