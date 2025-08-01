import BreadLogoWhiteIcon from '@/assets/bread-logo-white.svg';
import DetailIcon from '@/assets/detail-icon.svg';
import DownloadIcon from '@/assets/download-icon.svg';
import Button from '@/components/@common/Button/Button';
import Headline1 from '@/components/@common/Headline1/Headline1';
import Headline2 from '@/components/@common/Headline2/Headline2';
import Headline3 from '@/components/@common/Headline3/Headline3';
import IconButton from '@/components/@common/IconButton/IconButton';
import Paragraph from '@/components/@common/Paragraph/Paragraph';
import Layout from '@/layouts/Layout';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import * as S from './OrderPage.styled';

const OrderPage = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const [viewMode, setViewMode] = useState<'simple' | 'detail'>('simple');

  const simpleOrderItems = [
    { name: '아이스 아메리카노', quantity: 3 },
    { name: '복숭아 아이스티', quantity: 3 },
    { name: '초코칩 프라푸치노', quantity: 1 },
  ];

  const detailOrderItems = [
    { person: '다이앤', drink: '복숭아 아이스티' },
    { person: '메리', drink: '복숭아 아이스티' },
    { person: '니야', drink: '아이스 아메리카노' },
    { person: '엠제이', drink: '초코칩 프라푸치노' },
    { person: '꾹이', drink: '복숭아 아이스티' },
    { person: '한스', drink: '아이스 아메리카노' },
    { person: '루키', drink: '아이스 아메리카노' },
  ];

  const totalQuantity = simpleOrderItems.reduce((sum, item) => sum + item.quantity, 0);

  const handleToggle = () => {
    setViewMode((prev) => (prev === 'simple' ? 'detail' : 'simple'));
  };

  const renderSimpleView = () => (
    <>
      <S.OrderList>
        {simpleOrderItems.map((item, index) => (
          <S.OrderItem key={index}>
            <Paragraph>{item.name}</Paragraph>
            <Paragraph>{item.quantity}개</Paragraph>
          </S.OrderItem>
        ))}
      </S.OrderList>
      <S.Divider />
      <S.TotalWrapper>
        <Headline3>총 {totalQuantity}개</Headline3>
      </S.TotalWrapper>
    </>
  );

  const renderDetailView = () => (
    <S.DetailGrid>
      {detailOrderItems.map((item, index) => (
        <S.DetailItem key={index}>
          <Paragraph>{item.person}</Paragraph>
          <Paragraph>{item.drink}</Paragraph>
        </S.DetailItem>
      ))}
    </S.DetailGrid>
  );

  return (
    <Layout>
      <Layout.Banner>
        <S.BannerContent>
          <S.Logo src={BreadLogoWhiteIcon} />
          <Headline1 color="white">{location.state?.winner}</Headline1>
          <br />
          <Headline3 color="white">님이 당첨되었습니다!</Headline3>
        </S.BannerContent>
      </Layout.Banner>
      <Layout.Content>
        <S.ListHeader>
          <Headline2>주문 리스트 {viewMode === 'detail' ? '상세' : ''}</Headline2>
          <IconButton iconSrc={DetailIcon} onClick={handleToggle} />
        </S.ListHeader>
        {viewMode === 'simple' ? renderSimpleView() : renderDetailView()}
      </Layout.Content>
      <Layout.ButtonBar flexRatios={[5.5, 1]}>
        <Button variant="primary" onClick={() => navigate('/')}>
          메인 화면으로 가기
        </Button>
        <Button variant="primary" onClick={() => {}}>
          <img src={DownloadIcon} />
        </Button>
      </Layout.ButtonBar>
    </Layout>
  );
};

export default OrderPage;
