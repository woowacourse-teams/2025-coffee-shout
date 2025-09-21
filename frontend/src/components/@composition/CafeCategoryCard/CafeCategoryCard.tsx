import IconTextItem from '@/components/@common/IconTextItem/IconTextItem';
import * as S from './CafeCategoryCard.styled';
import CircleIcon from '@/components/@common/CircleIcon/CircleIcon';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';

type Props = {
  imageUrl: string;
  categoryName: string;
  onClick: () => void;
  color: string;
};

const CafeCategoryCard = ({ imageUrl, categoryName, onClick, color }: Props) => {
  const { isTouching, startTouchPress, endTouchPress } = useTouchInteraction();

  return (
    <S.Container
      onClick={onClick}
      $isTouching={isTouching}
      onTouchStart={startTouchPress}
      onTouchEnd={endTouchPress}
    >
      <IconTextItem
        iconContent={<CircleIcon imageUrl={imageUrl} color={color} />}
        textContent={<S.CategoryName>{categoryName}</S.CategoryName>}
        showBorder
      />
    </S.Container>
  );
};

export default CafeCategoryCard;
