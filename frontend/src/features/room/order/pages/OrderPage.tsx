import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import BreadLogoWhiteIcon from '@/assets/bread-logo-white.svg';
import DetailIcon from '@/assets/detail-icon.svg';
import DownloadIcon from '@/assets/download-icon.svg';
import Button from '@/components/@common/Button/Button';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline3 from '@/components/@common/Headline3/Headline3';
import IconButton from '@/components/@common/IconButton/IconButton';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import Layout from '@/layouts/Layout';
import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ParticipantResponse } from '../../lobby/pages/LobbyPage';
import * as S from './OrderPage.styled';
import MenuCount from '../components/MenuCount';
import PlayerMenu from '../components/PlayerMenu';

const OrderPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { send } = useWebSocket();
  const { joinCode } = useIdentifier();
  const [viewMode, setViewMode] = useState<'simple' | 'detail'>('simple');
  const [participants, setParticipants] = useState<ParticipantResponse>([]);

  const handleOrder = useCallback((data: ParticipantResponse) => {
    setParticipants(data);
  }, []);

  useWebSocketSubscription<ParticipantResponse>(`/room/${joinCode}`, handleOrder);

  const handleToggle = () => {
    setViewMode((prev) => (prev === 'simple' ? 'detail' : 'simple'));
  };

  useEffect(() => {
    if (joinCode) {
      send(`/room/${joinCode}/update-players`);
    }
  }, [joinCode, send]);

  return (
    <Layout>
      <Layout.Banner>
        <S.BannerContent>
          <S.Logo src={BreadLogoWhiteIcon} />
          <Headline1 color="white">{location.state?.winner}</Headline1>
          <br />
          <Headline3 color="white">님이 당첨되었습니다!</Headline3>
        </S.BannerContent>
      </Layout.Banner>
      <Layout.Content>
        <S.ListHeader>
          <Headline2>주문 리스트 {viewMode === 'detail' ? '상세' : ''}</Headline2>
          <IconButton iconSrc={DetailIcon} onClick={handleToggle} />
        </S.ListHeader>
        {viewMode === 'simple' ? (
          <MenuCount participants={participants} />
        ) : (
          <PlayerMenu participants={participants} />
        )}
      </Layout.Content>
      <Layout.ButtonBar flexRatios={[5.5, 1]}>
        <Button variant="primary" onClick={() => navigate('/')}>
          메인 화면으로 가기
        </Button>
        <Button variant="primary" onClick={() => {}}>
          <img src={DownloadIcon} />
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default OrderPage;
