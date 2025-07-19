import { Children, ReactElement } from 'react';
import * as S from './ButtonBar.styled';

type Props = {
  children: ReactElement | [ReactElement, ReactElement];
};

const ButtonBar = ({ children }: Props) => {
  const buttonCount = Array.isArray(children) ? children.length : 1;

  if (buttonCount === 1) {
    return <S.Container>{children}</S.Container>;
  }

  if (buttonCount === 2) {
    const [firstButton, secondButton] = Children.toArray(children);
    return (
      <S.Container>
        <S.FirstButtonWrapper>{firstButton}</S.FirstButtonWrapper>
        <S.SquareButtonWrapper>{secondButton}</S.SquareButtonWrapper>
      </S.Container>
    );
  }

  return <S.Container>{children}</S.Container>;
};

export default ButtonBar;
