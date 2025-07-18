import React, { PropsWithChildren } from 'react';
import * as S from './ButtonBar.styled';

const Button = ({ children }: PropsWithChildren) => {
  const buttonCount = Array.isArray(children) ? children.length : 1;

  if (buttonCount === 1) {
    return <S.ButtonContainer>{children}</S.ButtonContainer>;
  }

  if (buttonCount === 2) {
    const [firstButton, secondButton] = React.Children.toArray(children);
    return (
      <S.ButtonContainer>
        <S.FirstButtonWrapper>{firstButton}</S.FirstButtonWrapper>
        <S.SquareButtonWrapper>{secondButton}</S.SquareButtonWrapper>
      </S.ButtonContainer>
    );
  }

  return <S.ButtonContainer>{children}</S.ButtonContainer>;
};

export default Button;
