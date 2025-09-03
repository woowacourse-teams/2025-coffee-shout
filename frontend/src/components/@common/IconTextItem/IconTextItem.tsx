import { ReactNode, PropsWithChildren } from 'react';
import * as S from './IconTextItem.styled';

type Props = {
  iconComponent: ReactNode;
  textComponent: ReactNode;
  gap?: number;
} & PropsWithChildren;

const IconTextItem = ({ iconComponent, textComponent, gap = 20, children }: Props) => {
  return (
    <S.Container>
      <S.Wrapper gap={gap}>
        <S.IconWrapper>{iconComponent}</S.IconWrapper>
        <S.TextWrapper>{textComponent}</S.TextWrapper>
      </S.Wrapper>
      {children}
    </S.Container>
  );
};

export default IconTextItem;
