import Description from '@/components/@common/Description/Description';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as S from './MiniGameReadyPage.styled';

const INITIAL_COUNTDOWN_SECONDS = 3;
const COUNTDOWN_INTERVAL = 1000;

const MiniGameReadyPage = () => {
  const [countdown, setCountdown] = useState(INITIAL_COUNTDOWN_SECONDS);
  const navigate = useNavigate();
  const { roomId, miniGameId } = useParams();

  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          navigate(`/room/${roomId}/${miniGameId}/play`);
          return 0;
        }
        return prev - 1;
      });
    }, COUNTDOWN_INTERVAL);

    return () => clearInterval(timer);
  }, [navigate, roomId, miniGameId]);

  return (
    <Layout color="point-400">
      <S.Container>
        <S.Wrapper>
          <Headline1 color="white">곧 게임이 시작돼요</Headline1>
          <Description color="white">
            게임이 시작될 때까지
            <br />
            조금만 기다려주세요
          </Description>
        </S.Wrapper>
        <S.Timer>{countdown}</S.Timer>
      </S.Container>
    </Layout>
  );
};

export default MiniGameReadyPage;
