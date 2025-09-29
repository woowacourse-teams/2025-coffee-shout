import React from 'react';
import * as S from './Flip.styled';

type Props = {
  flipped: boolean;
  front: React.ReactNode;
  back: React.ReactNode;
};

const Flip = ({ flipped, front, back }: Props) => {
  return (
    <S.FlipWrapper>
      <S.Flipper flipped={flipped}>
        <S.Front>{front}</S.Front>
        <S.Back>{back}</S.Back>
      </S.Flipper>
    </S.FlipWrapper>
  );
};

export default Flip;
