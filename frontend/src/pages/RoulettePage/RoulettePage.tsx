import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import RouletteWheel from '@/components/@composition/RouletteWheel/RouletteWheel';
import Layout from '@/layouts/Layout';
import Button from '@/components/@common/Button/Button';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import * as S from './RoulettePage.styled';
import Description from '@/components/@common/Description/Description';
import Headline4 from '@/components/@common/Headline4/Headline4';

const RoulettePage = () => {
  const [spinning, setSpinning] = useState(false);
  const navigate = useNavigate();

  const handleSpinClick = () => {
    setSpinning(true);
    setTimeout(() => {
      setSpinning(false);
      navigate('/room/:roomId/roulette/result');
    }, 3000);
  };

  return (
    <Layout>
      <Layout.TopBar />
      <Layout.Content>
        <SectionTitle title="룰렛 현황" description="미니게임 결과에 따라 확률이 조정됩니다" />
        <RouletteWheel spinning={spinning} />
        <S.ProbabilityText>
          <Headline4>당첨 확률 +10%</Headline4>
        </S.ProbabilityText>
      </Layout.Content>
      <Layout.ButtonBar>
        <Button variant={spinning ? 'disabled' : 'primary'} onClick={handleSpinClick}>
          룰렛 돌리기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default RoulettePage;
