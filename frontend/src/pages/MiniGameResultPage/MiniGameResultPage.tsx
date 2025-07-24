import Button from '@/components/@common/Button/Button';
import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline3 from '@/components/@common/Headline3/Headline3';
import Headline4 from '@/components/@common/Headline4/Headline4';
import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import Layout from '@/layouts/Layout';
import { IconColor } from '@/types/player';
import { useNavigate, useParams } from 'react-router-dom';
import * as S from './MiniGameResultPage.styled';

const gameResults = [
  { id: 1, name: '다이앤', score: 20, iconColor: 'red' as IconColor, rank: 1 },
  { id: 2, name: '니야', score: 18, iconColor: 'red' as IconColor, rank: 2 },
  { id: 3, name: '메리', score: 15, iconColor: 'red' as IconColor, rank: 3 },
  { id: 4, name: '엠제이', score: 13, iconColor: 'red' as IconColor, rank: 4 },
  { id: 5, name: '루키', score: 10, iconColor: 'red' as IconColor, rank: 5 },
];

const MiniGameResultPage = () => {
  const navigate = useNavigate();
  const { roomId } = useParams<{ roomId: string }>();

  const handleViewRouletteResult = () => {
    if (roomId) {
      navigate(`/room/${roomId}/roulette`);
    }
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
        <Button variant="primary" onClick={handleViewRouletteResult}>
          룰렛 현황 보러가기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default MiniGameResultPage;
