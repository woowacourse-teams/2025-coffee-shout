import BreadLogoWhiteIcon from '@/assets/images/bread-logo-white.svg';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Headline3 from '@/components/@common/Headline3/Headline3';
import Layout from '@/layouts/Layout';
import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as S from './RouletteResultPage.styled';

const RouletteResultPage = () => {
  // TODO: 당첨자 받아오는 로직 구현
  const navigate = useNavigate();
  const { roomId } = useParams();

  const winner = '당첨자';

  useEffect(() => {
    const timer = setTimeout(() => {
      if (roomId) {
        navigate(`/room/${roomId}/order`);
      } else {
        navigate('/');
      }
    }, 3000);

    return () => clearTimeout(timer);
  }, [navigate, roomId]);

  return (
    <Layout color="point-400">
      <S.Container>
        <S.Logo src={BreadLogoWhiteIcon} />
        <Headline1 color="white">{winner}</Headline1>
        <Headline3 color="white">님이 당첨되었습니다!</Headline3>
      </S.Container>
    </Layout>
  );
};

export default RouletteResultPage;
