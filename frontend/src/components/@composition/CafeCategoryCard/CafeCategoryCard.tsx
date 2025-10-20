import { useButtonInteraction } from '@/hooks/useButtonInteraction';

import CircleIcon from '@/components/@common/CircleIcon/CircleIcon';
import IconTextItem from '@/components/@common/IconTextItem/IconTextItem';

import * as S from './CafeCategoryCard.styled';

type Props = {
  imageUrl: string;
  categoryName: string;
  onClick: () => void;
  color: string;
  position: number;
  totalCount: number;
};

const CafeCategoryCard = ({
  imageUrl,
  categoryName,
  onClick,
  color,
  position,
  totalCount,
}: Props) => {
  const { touchState, pointerHandlers } = useButtonInteraction({ onClick });

  return (
    <S.Container
      {...pointerHandlers}
      $touchState={touchState}
      aria-label={`${categoryName} 선택 버튼`}
      role="option"
      aria-posinset={position}
      aria-setsize={totalCount}
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
