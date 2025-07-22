import Layout from '@/layouts/Layout';
import { useEffect } from 'react';
import * as S from './Splash.styled';

type Props = {
  onComplete: () => void;
};

const Splash = ({ onComplete }: Props) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      onComplete();
    }, 2000);

    return () => clearTimeout(timer);
  }, [onComplete]);

  return (
    <Layout color="point-400">
      <S.Container>
        <S.LogoImage src="/images/logo-splash.svg" />
        <S.LogoText>커피빵</S.LogoText>
      </S.Container>
    </Layout>
  );
};

export default Splash;
