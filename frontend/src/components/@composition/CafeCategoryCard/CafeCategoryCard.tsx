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
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction();

  return (
    <S.Container
      onPointerUp={onClick}
      $touchState={touchState}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
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
