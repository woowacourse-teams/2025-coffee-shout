import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import RouletteIcon from '@/assets/roulette-icon.svg';
import StatisticsIcon from '@/assets/statistics-icon.svg';
import Button from '@/components/@common/Button/Button';
import IconButton from '@/components/@common/IconButton/IconButton';
import ProbabilityList from '@/components/@composition/ProbabilityList/ProbabilityList';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { Player } from '@/types/player';
import { RouletteView } from '@/types/roulette';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import RoulettePlaySection from '../../components/RoulettePlaySection/RoulettePlaySection';
import * as S from './RoulettePlayPage.styled';

const RoulettePage = () => {
  const navigate = useNavigate();
  const { playerType } = usePlayerType();
  const { joinCode, myName } = useIdentifier();
  const { send } = useWebSocket();

  const [isSpinning, setIsSpinning] = useState(false);
  const [currentView, setCurrentView] = useState<RouletteView>('roulette');
  const [winner, setWinner] = useState<string | null>(null);

  useWebSocketSubscription<Player>(`/room/${joinCode}/roulette`, (data) => {
    setWinner(data.playerName);
  });

  const isRouletteView = currentView === 'roulette';

  const getButtonVariant = () => {
    if (isSpinning) return 'disabled';
    if (playerType === 'GUEST') return 'loading';
    return 'primary';
  };

  const handleViewChange = () => {
    setCurrentView((prev) => (prev === 'roulette' ? 'statistics' : 'roulette'));
  };

  const handleSpinClick = () => {
    if (currentView === 'statistics') handleViewChange();
    setIsSpinning(true);
    send(`/room/${joinCode}/spin-roulette`, { hostName: myName });
  };

  useEffect(() => {
    // TODO: 당첨자가 나오지 않았을 때, 에러 처리 방식 정하기
    if (!winner || !winner.trim()) console.warn('당첨자가 추첨되지 않았습니다.');

    if (isSpinning) {
      const timer = setTimeout(() => {
        setIsSpinning(false);
        navigate(`/room/${joinCode}/roulette/result`, { state: { winner } });
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [isSpinning, winner, navigate, joinCode]);

  //TODO: 다른 에러 처리방식을 찾아보기
  if (!playerType) return null;

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
        <Button variant={getButtonVariant()} onClick={handleSpinClick}>
          룰렛 돌리기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default RoulettePage;
