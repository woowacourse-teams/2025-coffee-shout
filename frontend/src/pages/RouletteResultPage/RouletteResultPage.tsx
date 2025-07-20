import Layout from '@/layouts/Layout';
import * as S from './RouletteResultPage.styled';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Headline3 from '@/components/@common/Headline3/Headline3';
import { useLocation } from 'react-router-dom';

const RouletteResultPage = () => {
  const location = useLocation();
  const winnerName = location.state?.winnerName || '당첨자';

  return (
    <Layout color="point-400">
      <S.Container>
        <S.Logo src="/images/bread-logo-white.svg" />
        <Headline1 color="white">{winnerName}</Headline1>
        <Headline3 color="white">님이 당첨되었습니다!</Headline3>
      </S.Container>
    </Layout>
  );
};

export default RouletteResultPage;
