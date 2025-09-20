import LogoMainIcon from '@/assets/logo-main.svg';
import Headline3 from '@/components/@common/Headline3/Headline3';
import useModal from '@/components/@common/Modal/useModal';
import RoomActionButton from '@/components/@common/RoomActionButton/RoomActionButton';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { storageManager, STORAGE_KEYS, STORAGE_TYPES } from '@/utils/StorageManager';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import EnterRoomModal from '../components/EnterRoomModal/EnterRoomModal';
import Splash from '../components/Splash/Splash';
import * as S from './HomePage.styled';

const HomePage = () => {
  const navigate = useNavigate();
  const [showSplash, setShowSplash] = useState<boolean>(false);
  const { openModal, closeModal } = useModal();
  const { setHost, setGuest } = usePlayerType();
  const { clearIdentifier } = useIdentifier();
  const { isConnected, stopSocket } = useWebSocket();

  useEffect(() => {
    if (isConnected) {
      stopSocket();
    }
    clearIdentifier();
  }, [clearIdentifier, isConnected, stopSocket]);

  useEffect(() => {
    const checkFirstVisit = () => {
      const hasVisited = storageManager.getItem(
        STORAGE_KEYS.COFFEE_SHOUT_VISITED,
        STORAGE_TYPES.SESSION
      );

      if (!hasVisited) {
        setShowSplash(true);
        storageManager.setItem(STORAGE_KEYS.COFFEE_SHOUT_VISITED, 'true', STORAGE_TYPES.SESSION);
      }
    };
    checkFirstVisit();
  }, []);

  if (showSplash) {
    return <Splash onComplete={() => setShowSplash(false)} />;
  }
  const handleEnterRoom = () => {
    openModal(<EnterRoomModal onClose={closeModal} />, {
      title: '방 참가하기',
      showCloseButton: true,
    });
    setGuest();
  };

  const handleClickHostButton = () => {
    setHost();
    navigate('/entry/name');
  };

  return (
    <Layout>
      <Layout.Banner>
        <S.Banner>
          <Headline3 color="white">
            초대받은 방에 참가하거나
            <br />
            새로운 방을 만들어보세요
          </Headline3>
          <S.Logo src={LogoMainIcon} />
        </S.Banner>
      </Layout.Banner>
      <S.ButtonContainer>
        <RoomActionButton
          title="방 만들기"
          descriptions={['새로운 방을 만들어', '재미있는 커피내기를 시작해보세요 ']}
          onClick={handleClickHostButton}
        />
        <RoomActionButton
          title="방 참가하러 가기"
          descriptions={['받은 초대 코드를 입력해서', '방으로 들어가보세요']}
          onClick={handleEnterRoom}
        />
      </S.ButtonContainer>
    </Layout>
  );
};

export default HomePage;
