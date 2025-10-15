import Skeleton from '@/components/@common/Skeleton/Skeleton';
import * as S from './CafeCategoryCardSkeleton.styled';

const CafeCategoryCardSkeleton = () => {
  return (
    <S.Container>
      <S.Content>
        <S.IconWrapper>
          <Skeleton width={50} height={50} borderRadius="50%" />
        </S.IconWrapper>
        <S.TextWrapper>
          <Skeleton width="40%" height={18} />
        </S.TextWrapper>
      </S.Content>
    </S.Container>
  );
};

export default CafeCategoryCardSkeleton;
