import Description from '@/components/@common/Description/Description';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline4 from '@/components/@common/Headline4/Headline4';
import CardBack from '@/features/miniGame/cardGame/components/CardBack/CardBack';
import CircularProgress from '@/features/miniGame/cardGame/components/CircularProgress/CircularProgress';
import Layout from '@/layouts/Layout';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as S from './MiniGamePlayPage.styled';

const TOTAL_COUNT = 10;

// TODO: 게임 종류에 따라서 분기처리 되도록 수정 (이전 페이지에서 입력된 미니게임 종류를 토대로 화면이 바뀌어야 함 - 미니게임 종류에 대하여 Context로 관리 필요)
// TODO: 라운드가 총 2개이므로 2개의 라운드에 맞춰 이동 루트 추가
// TODO: 카드를 하나 선택했을 때 다음 페이지로 이동할 수 있도록 수정 (당장은 싱글 플레이이므로 이 로직 자체도 추후 수정되어야 함)

const MiniGamePlayPage = () => {
  const [current, setCurrent] = useState(TOTAL_COUNT);
  const navigate = useNavigate();
  const { roomId, miniGameId } = useParams();

  useEffect(() => {
    if (current > 0) {
      const timer = setTimeout(() => setCurrent(current - 1), 1000);
      return () => clearTimeout(timer);
    } else if (current === 0) {
      navigate(`/room/${roomId}/${miniGameId}/result`);
    }
  }, [current, miniGameId, navigate, roomId]);

  return (
    <Layout>
      <Layout.TopBar center={<Headline4>랜덤카드 게임</Headline4>} />
      <Layout.Content>
        <S.TitleContainer>
          <S.TitleWrapper>
            <Headline2>Round1</Headline2>
            <Description>카드를 골라주세요!</Description>
          </S.TitleWrapper>
          <S.CircularProgressWrapper>
            <CircularProgress current={current} total={TOTAL_COUNT} />
          </S.CircularProgressWrapper>
        </S.TitleContainer>
        <S.MyCardContainer>
          <CardBack size="medium" onClick={() => {}} />
          <CardBack size="medium" onClick={() => {}} />
        </S.MyCardContainer>
        <S.CardContainer>
          {Array.from({ length: 9 }, (_, index) => (
            <CardBack key={index} onClick={() => {}} />
          ))}
        </S.CardContainer>
      </Layout.Content>
    </Layout>
  );
};

export default MiniGamePlayPage;
