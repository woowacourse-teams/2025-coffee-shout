import Skeleton from '@/components/@common/Skeleton/Skeleton';
import * as S from './MenuListItemSkeleton.styled';

const MenuListItemSkeleton = () => {
  return Array.from({ length: 4 }).map((_, index) => (
    <S.Container key={index}>
      <Skeleton width="40%" height={20} />
    </S.Container>
  ));
};

export default MenuListItemSkeleton;
