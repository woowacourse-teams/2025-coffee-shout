import Button from '@/components/@common/Button/Button';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Layout from '@/layouts/Layout';
import * as S from './MiniGameResultPage.styled';
import { useNavigate, useParams } from 'react-router-dom';

const MiniGameResultPage = () => {
  const navigate = useNavigate();
  const { roomId } = useParams<{ roomId: string }>();

  const handleViewRouletteResult = () => {
    if (roomId) {
      navigate(`/room/${roomId}/roulette/result`);
    }
  };

  return (
    <Layout>
      <Layout.Banner height="30%">
        <S.Banner>
          <Headline2 color="white">게임 결과</Headline2>
          <S.Description>
            게임 결과를 통해
            <br />
            룰렛 가중치가 조정됩니다
          </S.Description>
        </S.Banner>
      </Layout.Banner>
      <Layout.Content></Layout.Content>
      <Layout.ButtonBar>
        <Button
          variant="primary"
          onClick={() => {
            handleViewRouletteResult();
          }}
        >
          룰렛 현황 보러가기
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default MiniGameResultPage;
