import React from 'react';
import * as S from './TopBar.styled';

type AlignType = 'start' | 'center' | 'end' | 'stretch';

type TopBarProps = {
  left?: React.ReactElement;
  center?: React.ReactElement;
  right?: React.ReactElement;
  align?: [AlignType, AlignType, AlignType];
};

const TopBar = ({ left, center, right, align = ['center', 'center', 'center'] }: TopBarProps) => {
  const [leftAlign, centerAlign, rightAlign] = align;

  return (
    <S.Container>
      <S.LeftSection $align={leftAlign}>{left}</S.LeftSection>
      <S.CenterSection $align={centerAlign}>{center}</S.CenterSection>
      <S.RightSection $align={rightAlign}>{right}</S.RightSection>
    </S.Container>
  );
};

export default TopBar;
