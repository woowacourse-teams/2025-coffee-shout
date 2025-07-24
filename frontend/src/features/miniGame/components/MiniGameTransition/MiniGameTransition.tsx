import { PropsWithChildren } from 'react';
import * as S from './MiniGameTransition.styled';
import Layout from '@/layouts/Layout';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Description from '@/components/@common/Description/Description';
import { RoundKey } from '@/types/round';

type Props = {
  prevRound: RoundKey;
} & PropsWithChildren;

const MiniGameTransition = ({ prevRound, children }: Props) => {
  return (
    <Layout color="point-400">
      <S.Container>
        <S.Wrapper>
          <S.DescriptionWrapper>
            <Headline1 color="white">Round {prevRound + 1}</Headline1>
            <Description color="white">다음 라운드로 이동합니다!</Description>
          </S.DescriptionWrapper>
          {children}
        </S.Wrapper>
      </S.Container>
    </Layout>
  );
};

export default MiniGameTransition;
