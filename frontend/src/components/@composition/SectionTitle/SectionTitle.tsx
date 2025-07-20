import React from 'react';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Description from '@/components/@common/Description/Description';
import * as S from './SectionTitle.styled';

type SectionTitleProps = {
  title: string;
  description: string;
  suffix?: React.ReactNode;
};

const SectionTitle = ({ title, description, suffix }: SectionTitleProps) => {
  return (
    <S.Container>
      <S.Wrapper>
        <Headline2>{title}</Headline2>
        {suffix}
      </S.Wrapper>
      <Description>{description}</Description>
    </S.Container>
  );
};

export default SectionTitle;
