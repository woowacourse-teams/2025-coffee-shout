import IconTextItem from '@/components/@common/IconTextItem/IconTextItem';
import * as S from './CafeCategoryCard.styled';
import CircleIcon from '@/components/@common/CircleIcon/CircleIcon';
import { COLOR_MAP } from '@/constants/color';

type Props = {
  imageUrl: string;
  categoryName: string;
  onClick: () => void;
};

const CafeCategoryCard = ({ imageUrl, categoryName, onClick }: Props) => {
  return (
    <S.Container onClick={onClick}>
      <IconTextItem
        iconContent={<CircleIcon imageUrl={imageUrl} color={COLOR_MAP['point-200']} />}
        textContent={<S.CategoryName>{categoryName}</S.CategoryName>}
        showBorder
      />
    </S.Container>
  );
};

export default CafeCategoryCard;
