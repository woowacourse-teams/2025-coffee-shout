import Description from '@/components/@common/Description/Description';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as S from './MiniGameReadyPage.styled';

const MiniGameReadyPage = () => {
  const [countdown, setCountdown] = useState(3);
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
    }, 1000);

    return () => clearInterval(timer);
  }, [navigate, roomId, miniGameId]);

  return (
    <Layout color="point-400">
      <S.Container>
        <S.TextContainer>
          <Headline1 color="white">곧 게임이 시작돼요</Headline1>
          <Description color="white">
            게임이 시작될 때까지
            <br />
            조금만 기다려주세요
          </Description>
        </S.TextContainer>
        <S.Time>{countdown}</S.Time>
      </S.Container>
    </Layout>
  );
};

export default MiniGameReadyPage;
