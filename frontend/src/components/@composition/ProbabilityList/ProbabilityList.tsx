import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import Divider from '@/components/@common/Divider/Divider';
import * as S from './ProbabilityList.styled';
import Headline4 from '@/components/@common/Headline4/Headline4';

const ProbabilityList = () => {
  return (
    <>
      <PlayerCard name="홍길동" iconColor="red">
        <Headline4>15%</Headline4>
      </PlayerCard>
      <S.DividerWrapper>
        <Divider />
      </S.DividerWrapper>

      <S.ScrollableWrapper>
        {['다이앤', '니야', '메리', '루키', '한스', '꾹이', '엠제이'].map((name) => (
          <PlayerCard key={name} name={name} iconColor="red">
            <Headline4>15%</Headline4>
          </PlayerCard>
        ))}
      </S.ScrollableWrapper>
      <S.BottomGap />
    </>
  );
};

export default ProbabilityList;
