import { ReactNode } from 'react';
import * as S from './Flip.styled';

type Props = {
  flipped: boolean;
  initialView: ReactNode;
  flippedView: ReactNode;
};

const Flip = ({ flipped, initialView, flippedView }: Props) => {
  return (
    <S.FlipWrapper>
      <S.Flipper flipped={flipped}>
        <S.InitialView>{initialView}</S.InitialView>
        <S.FlippedView>{flippedView}</S.FlippedView>
      </S.Flipper>
    </S.FlipWrapper>
  );
};

export default Flip;
