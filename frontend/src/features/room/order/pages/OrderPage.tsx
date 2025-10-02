import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import BreadLogoWhiteIcon from '@/assets/logo/bread-logo-white.png';
import Button from '@/components/@common/Button/Button';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline3 from '@/components/@common/Headline3/Headline3';
import TextButton from '@/components/@common/TextButton/TextButton';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import MenuCount from '../components/MenuCount/MenuCount';
import PlayerMenu from '../components/PlayerMenu/PlayerMenu';
import * as S from './OrderPage.styled';

const OrderPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { stopSocket, isConnected } = useWebSocket();
  const [viewMode, setViewMode] = useState<'simple' | 'detail'>('simple');

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
          <Headline1 color="white">{location.state?.winner}</Headline1>
          <Headline3 color="white">님이 당첨되었습니다!</Headline3>
        </S.BannerContent>
      </Layout.Banner>
      <Layout.Content>
        <S.ListHeader>
          <Headline2>주문 리스트 {viewMode === 'detail' ? '상세' : ''}</Headline2>
          <TextButton
            text={viewMode === 'detail' ? '요약 보기' : '상세 보기'}
            onClick={handleToggle}
          />
        </S.ListHeader>
        {viewMode === 'simple' ? <MenuCount /> : <PlayerMenu />}
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
