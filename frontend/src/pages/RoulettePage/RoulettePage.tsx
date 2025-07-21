import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import IconButton from '@/components/@common/IconButton/IconButton';
import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import RouletteWheel from '@/components/@composition/RouletteWheel/RouletteWheel';
import Layout from '@/layouts/Layout';
import Button from '@/components/@common/Button/Button';
import * as S from './RoulettePage.styled';

type RouletteView = 'roulette' | 'statistics';

const RoulettePage = () => {
  const navigate = useNavigate();

  const [currentView, setCurrentView] = useState<RouletteView>('roulette');

  const handleViewChange = () => {
    setCurrentView(currentView === 'roulette' ? 'statistics' : 'roulette');
  };

  const handleClickButton = () => {
    navigate('/room/:roomId/roulette/result');
  };

  return (
    <Layout>
      <Layout.TopBar />
      <Layout.Content>
        <S.Container>
          <SectionTitle
            title="룰렛 현황"
            description="이전 미니게임을 통해 당첨 확률이 조정되었습니다"
          />

          <S.IconButtonWrapper>
            <IconButton
              iconSrc={
                currentView === 'roulette'
                  ? '/images/statistics-icon.svg'
                  : '/images/roulette-icon.svg'
              }
              onClick={handleViewChange}
            />
          </S.IconButtonWrapper>
          {renderContent(currentView)}
        </S.Container>
      </Layout.Content>
      <Layout.ButtonBar flexRatios={[5.5, 1]}>
        <Button variant="primary" onClick={handleClickButton}>
          룰렛돌리기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default RoulettePage;

const renderContent = (currentView: RouletteView) => {
  switch (currentView) {
    case 'statistics':
      return <ProbabilityList />;
    case 'roulette':
    default:
      return <RouletteWheel />;
  }
};
