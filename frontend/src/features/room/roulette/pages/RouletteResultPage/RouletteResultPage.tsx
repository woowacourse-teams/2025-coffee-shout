import BreadLogoWhiteIcon from '@/assets/logo/bread-logo-white.png';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Headline3 from '@/components/@common/Headline3/Headline3';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import Layout from '@/layouts/Layout';
import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useCustomNavigate } from '@/hooks/useCustomNavigate';
import * as S from './RouletteResultPage.styled';

const RouletteResultPage = () => {
  const navigate = useCustomNavigate();
  const location = useLocation();
  const { joinCode } = useIdentifier();
  const winner = location.state?.winner ?? '알 수 없는 사용자';

  useEffect(() => {
    const timer = setTimeout(() => {
      if (joinCode) {
        navigate(`/room/${joinCode}/order`, {
          state: { winner },
        });
      } else {
        navigate('/');
      }
    }, 3000);

    return () => clearTimeout(timer);
  }, [navigate, joinCode, winner]);

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
