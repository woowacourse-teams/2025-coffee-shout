import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '@/layouts/Layout';
import Button from '@/components/@common/Button/Button';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import * as S from './RoulettePage.styled';
import IconButton from '@/components/@common/IconButton/IconButton';
import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import { RouletteView } from '@/types/roulette';
import RoulettePlaySection from './RoulettePlaySection/RoulettePlaySection';

const RoulettePage = () => {
  const navigate = useNavigate();

  const [spinning, setSpinning] = useState(false);
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');

  const isRouletteView = currentView === 'roulette';

  const handleSpinClick = () => {
    setSpinning(true);
    setTimeout(() => {
      setSpinning(false);
      navigate('/room/:roomId/roulette/result');
    }, 3000);
  };

  const handleViewChange = () => {
    setCurrentView((prev) => (prev === 'roulette' ? 'statistics' : 'roulette'));
  };

  return (
    <Layout>
      <Layout.TopBar />
      <Layout.Content>
        <S.Container>
          <SectionTitle title="룰렛 현황" description="미니게임 결과에 따라 확률이 조정됩니다" />
          {isRouletteView ? <RoulettePlaySection spinning={spinning} /> : <ProbabilityList />}
          <S.IconButtonWrapper>
            <IconButton
              iconSrc={
                isRouletteView ? './images/statistics-icon.svg' : './images/roulette-icon.svg'
              }
              onClick={handleViewChange}
            />
          </S.IconButtonWrapper>
        </S.Container>
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
