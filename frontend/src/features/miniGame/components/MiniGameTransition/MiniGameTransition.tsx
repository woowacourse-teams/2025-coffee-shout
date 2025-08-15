import Description from '@/components/@common/Description/Description';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Layout from '@/layouts/Layout';
import { ROUND_MAP, RoundType } from '@/types/miniGame/round';
import { PropsWithChildren } from 'react';
import * as S from './MiniGameTransition.styled';

type Props = {
  currentRound: RoundType;
} & PropsWithChildren;

const MiniGameTransition = ({ currentRound, children }: Props) => {
  return (
    <Layout color="point-400">
      <S.Container>
        <S.Wrapper>
          <S.DescriptionWrapper>
            <Headline1 color="white">Round {ROUND_MAP[currentRound]}</Headline1>
            <Description color="white">다음 라운드로 이동합니다!</Description>
          </S.DescriptionWrapper>
          {children}
        </S.Wrapper>
      </S.Container>
    </Layout>
  );
};

export default MiniGameTransition;
