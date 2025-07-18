import Button from '@/components/@common/Button/Button';
import Layout from '../../layouts/Layout';
import Headline2 from '@/components/@common/Headline2/Headline2';
import ProgressCounter from '@/components/@common/ProgressCounter/ProgressCounter';
import Description from '@/components/@common/Description/Description';
import Title from '@/layouts/contentLayouts/Title/Title';
import Info from '@/layouts/contentLayouts/Info/Info';

const HomePage = () => {
  return (
    <Layout>
      {/* <Layout.Top /> */}
      <Layout.Banner />
      <Layout.Content>
        <Title>
          <div style={{ display: 'flex' }}>
            <Headline2>타이틀</Headline2>
            <ProgressCounter current={1} total={10} />
          </div>
        </Title>
        <Info>
          <Description color="gray-500">음료 아이콘을 누르면 음료를 변경할 수 있습니다</Description>
        </Info>
        <div>참가자리스트</div>
      </Layout.Content>
      <Layout.ButtonBar>
        <Button variant="primary" width="100%" height="large">
          버튼튼
        </Button>
        <Button variant="primary" width="100%" height="large">
          버튼튼
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default HomePage;
