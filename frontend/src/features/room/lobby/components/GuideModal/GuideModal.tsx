import { useState } from 'react';
import * as S from './GuideModal.styled';
import GuideContent from './GuideContent/GuideContent';
import Pagination from './Pagination/Pagination';
import Layout from '@/layouts/Layout';
import Headline3 from '@/components/@common/Headline3/Headline3';

type Props = {
  onClose: () => void;
};

export type GuideInfo = {
  title: string;
  description: string;
  image: string; // @TODO: 추후에 gif 수정
};

const GUIDE_PAGES: GuideInfo[] = [
  {
    title: '참여자들과 함께 모여보세요!',
    description: '로비에서 친구들과 함께 참여하고\n원하는 미니게임을 선택해보세요',
    image: 'lobby-screen',
  },
  {
    title: '미니게임을 즐겨보세요!',
    description: '선택한 미니게임을 친구들과 함께 플레이하고\n최선을 다해 좋은 결과를 만들어보세요',
    image: 'game-screen',
  },
  {
    title: '게임 결과를 확인하세요!',
    description: '모든 참여자의 게임 결과와 순위를\n한눈에 확인할 수 있습니다',
    image: 'result-screen',
  },
  {
    title: '행운의 룰렛을 돌려보세요!',
    description: '게임 결과를 바탕으로 룰렛이 돌아가며\n누가 당첨될지 기대해보세요',
    image: 'roulette-screen',
  },
  {
    title: '당첨자와 주문을 확인하세요!',
    description:
      '룰렛 결과로 선정된 당첨자를 확인하고\n주문 목록을 보며 맛있는 커피를 기다려보세요',
    image: 'order-screen',
  },
];

const GuideModal = ({ onClose }: Props) => {
  const [currentPage, setCurrentPage] = useState(0);

  const handlePrevious = () => {
    setCurrentPage((prev) => Math.max(0, prev - 1));
  };

  const handleNext = () => {
    if (currentPage + 1 === GUIDE_PAGES.length) onClose();
    setCurrentPage((prev) => Math.min(GUIDE_PAGES.length - 1, prev + 1));
  };

  const handleClose = () => {
    onClose();
  };

  return (
    <Layout padding="0px">
      <Layout.TopBar
        center={<Headline3>커피빵 시작하기</Headline3>}
        right={<S.CloseButton onClick={handleClose}>건너뛰기</S.CloseButton>}
      />
      <Layout.Content>
        <GuideContent pageData={GUIDE_PAGES[currentPage]} />
        <Pagination
          currentPage={currentPage}
          totalPages={GUIDE_PAGES.length}
          onPrevious={handlePrevious}
          onNext={handleNext}
        />
      </Layout.Content>
    </Layout>
  );
};

export default GuideModal;
