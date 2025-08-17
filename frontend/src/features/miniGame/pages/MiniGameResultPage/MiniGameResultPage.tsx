import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import Button from '@/components/@common/Button/Button';
import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline3 from '@/components/@common/Headline3/Headline3';
import Headline4 from '@/components/@common/Headline4/Headline4';
import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import Layout from '@/layouts/Layout';
import { useProbabilityHistory } from '@/contexts/ProbabilityHistory/ProbabilityHistoryContext';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import { colorList } from '@/constants/color';
import { MiniGameType, PlayerRank, PlayerScore } from '@/types/miniGame';
import { Probability } from '@/types/roulette';
import * as S from './MiniGameResultPage.styled';
import { useParticipants } from '@/contexts/Participants/ParticipantsContext';

type RankResponse = {
  ranks: PlayerRank[];
};
type ScoreResponse = {
  scores: PlayerScore[];
};

const MiniGameResultPage = () => {
  const navigate = useNavigate();
  const miniGameType = useParams<{ miniGameType: MiniGameType }>().miniGameType;
  const { send } = useWebSocket();
  const { myName, joinCode } = useIdentifier();
  const { playerType } = usePlayerType();
  const { getParticipantColorIndex } = useParticipants();
  const { updateCurrentProbabilities } = useProbabilityHistory();
  const [ranks, setRanks] = useState<PlayerRank[] | null>(null);
  const [scores, setScores] = useState<PlayerScore[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const handlePlayerProbabilitiesData = useCallback(
    (data: Probability[]) => {
      const playerProbabilitiesData = data.map((item) => ({
        playerName: item.playerResponse.playerName,
        probability: item.probability,
        playerColor: colorList[item.playerResponse.colorIndex],
      }));

      updateCurrentProbabilities(playerProbabilitiesData);

      if (joinCode) {
        navigate(`/room/${joinCode}/roulette/play`);
      }
    },
    [joinCode, navigate, updateCurrentProbabilities]
  );

  useWebSocketSubscription<Probability[]>(
    `/room/${joinCode}/roulette`,
    handlePlayerProbabilitiesData
  );

  const handleViewRouletteResult = () => {
    send(`/room/${joinCode}/get-probabilities`);
  };

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);

        const { ranks } = await api.get<RankResponse>(
          `/minigames/ranks?joinCode=${joinCode}&miniGameType=${miniGameType}`
        );
        const { scores } = await api.get<ScoreResponse>(
          `/minigames/scores?joinCode=${joinCode}&miniGameType=${miniGameType}`
        );
        ranks.sort((a, b) => a.rank - b.rank);
        setRanks(ranks);
        setScores(scores);
      } catch (error) {
        if (error instanceof ApiError) {
          setError(error.message);
        } else if (error instanceof NetworkError) {
          setError('네트워크 연결을 확인해주세요');
        } else {
          setError('알 수 없는 오류가 발생했습니다');
        }
      } finally {
        setLoading(false);
      }
    })();
  }, [joinCode, miniGameType]);

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
        {loading ? (
          <div>로딩 중...</div>
        ) : error ? (
          <div>{error}</div>
        ) : ranks && scores ? (
          <S.ResultList>
            {ranks.map((playerRank) => (
              <S.PlayerCardWrapper
                key={playerRank.playerName}
                isHighlighted={playerRank.playerName === myName}
              >
                <Headline3>
                  <S.RankNumber rank={playerRank.rank}>{playerRank.rank}</S.RankNumber>
                </Headline3>
                <PlayerCard
                  name={playerRank.playerName}
                  playerColor={colorList[getParticipantColorIndex(playerRank.playerName)]}
                >
                  <Headline4>
                    {scores.find((score) => score.playerName === playerRank.playerName)?.score}점
                  </Headline4>
                </PlayerCard>
              </S.PlayerCardWrapper>
            ))}
          </S.ResultList>
        ) : null}
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
