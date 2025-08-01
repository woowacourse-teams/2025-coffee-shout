import Description from '@/components/@common/Description/Description';
import Headline1 from '@/components/@common/Headline1/Headline1';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as S from './MiniGameReadyPage.styled';
import { useCardGame } from '@/contexts/CardGame/CardGameContext';

const INITIAL_COUNTDOWN_SECONDS = 3;
const COUNTDOWN_INTERVAL = 1000;

const MiniGameReadyPage = () => {
  const { startCardGame } = useCardGame();
  const [countdown, setCountdown] = useState(INITIAL_COUNTDOWN_SECONDS);
  const navigate = useNavigate();
  const { miniGameType } = useParams();
  const { joinCode } = useIdentifier();

  useEffect(() => {
    if (countdown <= 1) return;

    const timer = setInterval(() => {
      setCountdown((prev) => prev - 1);
    }, COUNTDOWN_INTERVAL);

    return () => clearInterval(timer);
  }, [countdown]);

  useEffect(() => {
    if (startCardGame) {
      navigate(`/room/${joinCode}/${miniGameType}/play`);
    }
  }, [joinCode, miniGameType, startCardGame, navigate]);

  return (
    <Layout color="point-400">
      <S.Container>
        <S.Wrapper>
          <Headline1 color="white">곧 게임이 시작돼요</Headline1>
          <S.DescriptionWrapper>
            <Description color="white">게임이 시작될 때까지</Description>
            <Description color="white">조금만 기다려주세요</Description>
          </S.DescriptionWrapper>
        </S.Wrapper>
        <S.Timer>{countdown}</S.Timer>
      </S.Container>
    </Layout>
  );
};

export default MiniGameReadyPage;
