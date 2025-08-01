import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import Button from '@/components/@common/Button/Button';
import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline3 from '@/components/@common/Headline3/Headline3';
import Headline4 from '@/components/@common/Headline4/Headline4';
import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import { ColorList } from '@/constants/color';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import Layout from '@/layouts/Layout';
import { Probability } from '@/types/roulette';
import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import * as S from './MiniGameResultPage.styled';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { useCardGame } from '@/contexts/CardGame/CardGameContext';

const MiniGameResultPage = () => {
  const navigate = useNavigate();
  const { send } = useWebSocket();
  const { myName, joinCode } = useIdentifier();
  const { playerType } = usePlayerType();
  const { ranks, scores } = useCardGame();

  const handlePlayerProbabilitiesData = useCallback(
    (data: Probability[]) => {
      const playerProbabilitiesData = data.map((item) => ({
        playerName: item.playerResponse.playerName,
        probability: item.probability,
      }));

      if (joinCode) {
        navigate(`/room/${joinCode}/roulette/play`, {
          state: { playerProbabilitiesData },
        });
      }
    },
    [joinCode, navigate]
  );

  useWebSocketSubscription<Probability[]>(
    `/room/${joinCode}/roulette`,
    handlePlayerProbabilitiesData
  );

  const handleViewRouletteResult = () => {
    send(`/room/${joinCode}/get-probabilities`);
  };

  return (
    <Layout>
      <Layout.Banner height="30%">
        <S.Banner>
          <Headline2 color="white">게임 결과</Headline2>
          <S.DescriptionWrapper>
            <Description color="white">게임 결과를 통해</Description>
            <Description color="white">룰렛 가중치가 조정됩니다</Description>
          </S.DescriptionWrapper>
        </S.Banner>
      </Layout.Banner>
      <Layout.Content>
        <S.ResultList>
          {ranks.map((playerRank) => (
            <S.PlayerCardWrapper
              key={playerRank.playerName}
              isHighlighted={playerRank.playerName === myName}
            >
              <Headline3>
                <S.RankNumber rank={playerRank.rank}>{playerRank.rank}</S.RankNumber>
              </Headline3>
              <PlayerCard name={playerRank.playerName} iconColor={'#FF6B6B'}>
                <Headline4>
                  {scores.find((score) => score.playerName === playerRank.playerName)?.score}점
                </Headline4>
              </PlayerCard>
            </S.PlayerCardWrapper>
          ))}
        </S.ResultList>
      </Layout.Content>
      <Layout.ButtonBar>
        {playerType === 'HOST' ? (
          <Button variant="primary" onClick={handleViewRouletteResult}>
            룰렛 현황 보러가기
          </Button>
        ) : (
          <Button variant="loading">대기 중</Button>
        )}
      </Layout.ButtonBar>
    </Layout>
  );
};

export default MiniGameResultPage;
