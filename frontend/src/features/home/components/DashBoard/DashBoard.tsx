import Top3WinnersSlide from '@/components/@composition/Top3WinnersSlide/Top3WinnersSlide';

const DashBoard = () => {
  return (
    <Top3WinnersSlide
      winners={[
        { name: '세라', count: 20 },
        { name: '민수', count: 15 },
        { name: '지영', count: 12 },
      ]}
    />
  );
};

export default DashBoard;
