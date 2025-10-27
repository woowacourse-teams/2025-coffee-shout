import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import BreadLogoWhiteIcon from '@/assets/logo/bread-logo-white.png';
import Button from '@/components/@common/Button/Button';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline3 from '@/components/@common/Headline3/Headline3';
import TextButton from '@/components/@common/TextButton/TextButton';
import { useReplaceNavigate } from '@/hooks/useReplaceNavigate';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import MenuCount from '../components/MenuCount/MenuCount';
import PlayerMenu from '../components/PlayerMenu/PlayerMenu';
import * as S from './OrderPage.styled';

type ViewMode = 'simple' | 'detail';

const VIEW_MODE_CONFIG = {
  simple: {
    title: '주문 리스트',
    buttonText: '참가자별 보기',
    ViewComponent: MenuCount,
  },
  detail: {
    title: '주문 리스트 상세',
    buttonText: '메뉴별 보기',
    ViewComponent: PlayerMenu,
  },
} as const;

const OrderPage = () => {
  const navigate = useReplaceNavigate();
  const location = useLocation();
  const { stopSocket, isConnected } = useWebSocket();
  const winner = location.state?.winner ?? '알 수 없는 사용자';

  const [viewMode, setViewMode] = useState<ViewMode>('simple');
  const { title, buttonText, ViewComponent } = VIEW_MODE_CONFIG[viewMode];

  const handleToggle = () => {
    setViewMode((prev) => (prev === 'simple' ? 'detail' : 'simple'));
  };

  const handleClickGoMain = () => {
    navigate('/');
  };

  useEffect(() => {
    if (isConnected) {
      stopSocket();
    }
  }, [stopSocket, isConnected]);

  return (
    <Layout>
      <Layout.Banner>
        <S.BannerContent>
          <S.Logo src={BreadLogoWhiteIcon} />
          <Headline1 color="white">{winner}</Headline1>
          <Headline3 color="white">님이 당첨되었습니다!</Headline3>
        </S.BannerContent>
      </Layout.Banner>
      <Layout.Content>
        <S.ListHeader>
          <Headline2>{title}</Headline2>
          <TextButton text={buttonText} onClick={handleToggle} />
        </S.ListHeader>
        <ViewComponent />
      </Layout.Content>
      <Layout.ButtonBar>
        <Button variant="primary" onClick={handleClickGoMain}>
          메인 화면으로 가기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default OrderPage;
