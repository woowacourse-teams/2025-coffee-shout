import { usePressAnimation } from '@/hooks/usePressAnimation';

import CircleIcon from '@/components/@common/CircleIcon/CircleIcon';
import IconTextItem from '@/components/@common/IconTextItem/IconTextItem';

import * as S from './CafeCategoryCard.styled';

type Props = {
  imageUrl: string;
  categoryName: string;
  onClick: () => void;
  color: string;
};

const CafeCategoryCard = ({ imageUrl, categoryName, onClick, color }: Props) => {
  const { touchState, onPointerDown, onPointerUp } = usePressAnimation();

  return (
    <S.Container
      onPointerDown={onPointerDown}
      onPointerUp={(e) => {
        onPointerUp(e);
        onClick();
      }}
      $touchState={touchState}
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
