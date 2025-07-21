import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import Divider from '@/components/@common/Divider/Divider';
import * as S from './ProbabilityList.styled';

const ProbabilityList = () => {
  return (
    <>
      <PlayerCard name="홍길동" iconColor="red">
        <S.ProbabilityText>15%</S.ProbabilityText>
      </PlayerCard>
      <S.DividerWrapper>
        <Divider />
      </S.DividerWrapper>

      <S.ScrollableWrapper>
        {['다이앤', '니야', '메리', '루키', '한스', '꾹이', '엠제이'].map((name) => (
          <PlayerCard key={name} name={name} iconColor="red">
            <S.ProbabilityText>15%</S.ProbabilityText>
          </PlayerCard>
        ))}
      </S.ScrollableWrapper>
      <S.BottomGap />
    </>
  );
};

export default ProbabilityList;
