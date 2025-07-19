import React from 'react';
import * as S from './ButtonBar.styled';

type Props = {
  children: React.ReactElement | [React.ReactElement, React.ReactElement];
};

const ButtonBar = ({ children }: Props) => {
  const buttonCount = Array.isArray(children) ? children.length : 1;

  if (buttonCount === 1) {
    return <S.Container>{children}</S.Container>;
  }

  if (buttonCount === 2) {
    const [firstButton, secondButton] = React.Children.toArray(children);
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
