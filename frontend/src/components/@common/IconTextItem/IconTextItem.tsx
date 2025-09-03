import { ReactNode } from 'react';
import * as S from './IconTextItem.styled';

type Props = {
  iconContent: ReactNode;
  textContent: ReactNode;
  rightContent?: ReactNode;
  gap?: number;
  showBorder?: boolean;
};

const IconTextItem = ({
  iconContent,
  textContent,
  rightContent,
  gap = 20,
  showBorder = false,
}: Props) => {
  return (
    <S.Container $showBorder={showBorder}>
      <S.Wrapper $gap={gap}>
        <S.IconWrapper>{iconContent}</S.IconWrapper>
        <S.TextWrapper>{textContent}</S.TextWrapper>
      </S.Wrapper>
      {rightContent}
    </S.Container>
  );
};

export default IconTextItem;
