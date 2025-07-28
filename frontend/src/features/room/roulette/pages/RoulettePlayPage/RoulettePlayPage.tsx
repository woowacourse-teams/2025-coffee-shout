import RouletteIcon from '@/assets/roulette-icon.svg';
import StatisticsIcon from '@/assets/statistics-icon.svg';
import Button from '@/components/@common/Button/Button';
import IconButton from '@/components/@common/IconButton/IconButton';
import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import RoulettePlaySection from '../../components/RoulettePlaySection/RoulettePlaySection';
import Layout from '@/layouts/Layout';
import { useUserRole } from '@/contexts/UserRoleContext';
import { RouletteView } from '@/types/roulette';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './RoulettePlayPage.styled';

const RoulettePage = () => {
  const navigate = useNavigate();

  const { userRole } = useUserRole();

  const [isSpinning, setIsSpinning] = useState(false);
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');

  const isRouletteView = currentView === 'roulette';

  const buttonVariant = isSpinning ? 'disabled' : userRole === 'GUEST' ? 'loading' : 'primary';

  //TODO: 다른 에러 처리방식을 찾아보기
  if (!userRole) return null;

  const handleViewChange = () => {
    setCurrentView((prev) => (prev === 'roulette' ? 'statistics' : 'roulette'));
  };

  const handleSpinClick = () => {
    if (currentView === 'statistics') handleViewChange();
    setIsSpinning(true);
  };

  useEffect(() => {
    if (isSpinning) {
      const timer = setTimeout(() => {
        setIsSpinning(false);
        navigate('/room/:roomId/roulette/result');
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [navigate, isSpinning]);

  return (
    <Layout>
      <Layout.TopBar />
      <Layout.Content>
        <S.Container>
          <SectionTitle title="룰렛 현황" description="미니게임 결과에 따라 확률이 조정됩니다" />
          {isRouletteView ? <RoulettePlaySection isSpinning={isSpinning} /> : <ProbabilityList />}
          <S.IconButtonWrapper>
            <IconButton
              iconSrc={isRouletteView ? StatisticsIcon : RouletteIcon}
              onClick={handleViewChange}
            />
          </S.IconButtonWrapper>
        </S.Container>
      </Layout.Content>
      <Layout.ButtonBar>
        <Button variant={buttonVariant} onClick={handleSpinClick}>
          룰렛 돌리기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default RoulettePage;
