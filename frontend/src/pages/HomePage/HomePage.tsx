import Headline3 from '@/components/@common/Headline3/Headline3';
import Layout from '@/layouts/Layout';
import * as S from './HomePage.styled';
import RoomActionButton from '@/components/@common/RoomActionButton/RoomActionButton';

const HomePage = () => {
  return (
    <Layout>
      <Layout.Banner>
        <S.BannerContainer>
          <Headline3 color="white">
            초대받은 방에 참가하거나
            <br />
            새로운 방을 만들어보세요
          </Headline3>
          <S.Logo src="/images/logo-main.svg" />
        </S.BannerContainer>
      </Layout.Banner>
      <S.ButtonContainer>
        <RoomActionButton
          title="방 만들기"
          descriptions={['새로운 방을 만들어', '재미있는 커피내기를 시작해보세요 ']}
          onClick={() => {}}
        />
        <RoomActionButton
          title="방 참가하러 가기"
          descriptions={['받은 초대 코드를 입력해서', '방으로 들어가보세요']}
          onClick={() => {}}
        />
      </S.ButtonContainer>
    </Layout>
  );
};

export default HomePage;
