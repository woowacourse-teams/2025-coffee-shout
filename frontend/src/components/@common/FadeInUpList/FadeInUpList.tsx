import { ReactNode } from 'react';
import * as S from './FadeInUpList.styled';

type FadeInUpListProps<T> = {
  items: T[];
  renderItem: (item: T, index: number) => ReactNode;
  staggerDelay?: number; // default 200ms
  animationDuration?: number; // default 600ms
  getItemKey?: (item: T, index: number) => string | number;
};

const FadeInUpList = <T,>({
  items,
  renderItem,
  staggerDelay = 200,
  animationDuration = 600,
  getItemKey,
}: FadeInUpListProps<T>) => {
  return (
    <>
      {items.map((item, index) => {
        const key = getItemKey ? getItemKey(item, index) : index;
        return (
          <S.ItemWrapper
            key={key}
            $index={index}
            $staggerDelay={staggerDelay}
            $animationDuration={animationDuration}
          >
            {renderItem(item, index)}
          </S.ItemWrapper>
        );
      })}
    </>
  );
};

export default FadeInUpList;
