import IconTextItem from '@/components/@common/IconTextItem/IconTextItem';
import * as S from './CafeCategoryCard.styled';
import CircleIcon from '@/components/@common/CircleIcon/CircleIcon';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import { MouseEvent, TouchEvent } from 'react';

type Props = {
  imageUrl: string;
  categoryName: string;
  onClick: (e: TouchEvent<HTMLButtonElement> | MouseEvent<HTMLButtonElement>) => void;
  color: string;
};

const CafeCategoryCard = ({ imageUrl, categoryName, onClick, color }: Props) => {
  const { touchState, handleTouchStart, handleTouchEnd } = useTouchInteraction({ onClick });

  return (
    <S.Container
      onClick={onClick}
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
