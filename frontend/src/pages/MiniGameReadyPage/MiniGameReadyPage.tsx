import Headline1 from '@/components/@common/Headline1/Headline1';
import Layout from '@/layouts/Layout';
import * as S from './MiniGameReadyPage.styled';
import Description from '@/components/@common/Description/Description';

const MiniGameReadyPage = () => {
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
        <S.Time>3</S.Time>
      </S.Container>
    </Layout>
  );
};

export default MiniGameReadyPage;
