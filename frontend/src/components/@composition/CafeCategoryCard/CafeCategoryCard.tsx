import { useButtonInteraction } from '@/hooks/useButtonInteraction';

import CircleIcon from '@/components/@common/CircleIcon/CircleIcon';
import IconTextItem from '@/components/@common/IconTextItem/IconTextItem';

import * as S from './CafeCategoryCard.styled';

type Props = {
  imageUrl: string;
  categoryName: string;
  onClick: () => void;
  color: string;
  ariaLabel?: string;
};

const CafeCategoryCard = ({ imageUrl, categoryName, onClick, color, ariaLabel }: Props) => {
  const { touchState, pointerHandlers } = useButtonInteraction({ onClick });

  return (
    <S.Container
      {...pointerHandlers}
      $touchState={touchState}
      aria-label={ariaLabel || `${categoryName} 선택 버튼`}
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
