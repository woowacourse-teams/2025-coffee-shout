import IconTextItem from '@/components/@common/IconTextItem/IconTextItem';
import * as S from './CafeCategoryCard.styled';
import CircleIcon from '@/components/@common/CircleIcon/CircleIcon';
import { useTouchInteraction } from '@/hooks/useTouchInteraction';
import { TouchEvent } from 'react';
import { checkIsTouchDevice } from '@/utils/checkIsTouchDevice';

type Props = {
  imageUrl: string;
  categoryName: string;
  onClick: () => void;
  color: string;
};

const CafeCategoryCard = ({ imageUrl, categoryName, onClick, color }: Props) => {
  const { isTouching, startTouchPress, endTouchPress } = useTouchInteraction();
  const isTouchDevice = checkIsTouchDevice();

  const handleTouchStart = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouchDevice) return;

    e.preventDefault();
    startTouchPress();
  };

  const handleTouchEnd = (e: TouchEvent<HTMLButtonElement>) => {
    if (!isTouchDevice) return;

    e.preventDefault();
    endTouchPress(onClick, e);
  };

  return (
    <S.Container
      onClick={onClick}
      $isTouching={isTouching}
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
