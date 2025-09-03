import { ReactNode } from 'react';
import * as S from './IconTextItem.styled';

type Props = {
  iconContent: ReactNode;
  textContent: ReactNode;
  rightContent?: ReactNode;
  gap?: number;
  showBorder?: boolean;
  onClick?: () => void;
};

const IconTextItem = ({
  iconContent,
  textContent,
  rightContent,
  gap = 20,
  showBorder = false,
  onClick,
}: Props) => {
  return (
    <S.Container $showBorder={showBorder} onClick={onClick}>
      <S.Wrapper $gap={gap}>
        <S.IconWrapper>{iconContent}</S.IconWrapper>
        <S.TextWrapper>{textContent}</S.TextWrapper>
      </S.Wrapper>
      {rightContent}
    </S.Container>
  );
};

export default IconTextItem;
