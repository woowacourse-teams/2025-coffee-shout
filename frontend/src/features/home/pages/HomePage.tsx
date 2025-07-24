import LogoMainIcon from '@/assets/logo-main.svg';
import Headline3 from '@/components/@common/Headline3/Headline3';
import RoomActionButton from '@/components/@common/RoomActionButton/RoomActionButton';
import useModal from '@/features/ui/Modal/useModal';
import Layout from '@/layouts/Layout';
import Splash from '../components/Splash/Splash';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import EnterRoomModal from '../components/EnterRoomModal/EnterRoomModal';
import * as S from './HomePage.styled';

const HomePage = () => {
  const navigate = useNavigate();
  const [showSplash, setShowSplash] = useState<boolean>(false);
  const { openModal, closeModal } = useModal();

  useEffect(() => {
    const checkFirstVisit = () => {
      try {
        const hasVisited = sessionStorage.getItem('coffee-shout-visited');

        if (!hasVisited) {
          setShowSplash(true);
          sessionStorage.setItem('coffee-shout-visited', 'true');
        }
      } catch (error) {
        console.error('sessionStorage 오류 : ', error);
        setShowSplash(true);
      }
    };

    checkFirstVisit();
  }, []);

  const handleEnterRoom = () => {
    openModal(<EnterRoomModal onClose={closeModal} />, {
      title: '방 참가하기',
      showCloseButton: true,
    });
  };

  if (showSplash) {
    return <Splash onComplete={() => setShowSplash(false)} />;
  }

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
          onClick={() => {
            navigate('/entry/name');
          }}
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
