import { ReactNode } from 'react';
import * as S from './IconTextItem.styled';

type Props = {
  iconContent: ReactNode;
  textContent: ReactNode;
  rightContent?: ReactNode;
  gap?: number;
};

const IconTextItem = ({ iconContent, textContent, rightContent, gap = 20 }: Props) => {
  return (
    <S.Container>
      <S.Wrapper gap={gap}>
        <S.IconWrapper>{iconContent}</S.IconWrapper>
        <S.TextWrapper>{textContent}</S.TextWrapper>
      </S.Wrapper>
      {rightContent}
    </S.Container>
  );
};

export default IconTextItem;
