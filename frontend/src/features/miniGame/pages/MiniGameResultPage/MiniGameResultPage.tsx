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

const gameResults = [
  { id: 1, name: '다이앤', score: 20, iconColor: '#FF6B6B' as ColorList, rank: 1 },
  { id: 2, name: '니야', score: 18, iconColor: '#FF6B6B' as ColorList, rank: 2 },
  { id: 3, name: '메리', score: 15, iconColor: '#FF6B6B' as ColorList, rank: 3 },
  { id: 4, name: '엠제이', score: 13, iconColor: '#FF6B6B' as ColorList, rank: 4 },
  { id: 5, name: '루키', score: 10, iconColor: '#FF6B6B' as ColorList, rank: 5 },
];

const MiniGameResultPage = () => {
  const navigate = useNavigate();
  const { send } = useWebSocket();
  const { joinCode } = useIdentifier();
  const { playerType } = usePlayerType();

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
          {gameResults.map((player) => (
            <S.PlayerCardWrapper key={player.id} isHighlighted={player.rank === 4}>
              <Headline3>
                <S.RankNumber rank={player.rank}>{player.rank}</S.RankNumber>
              </Headline3>
              <PlayerCard name={player.name} iconColor={player.iconColor}>
                <Headline4>{player.score}점</Headline4>
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
