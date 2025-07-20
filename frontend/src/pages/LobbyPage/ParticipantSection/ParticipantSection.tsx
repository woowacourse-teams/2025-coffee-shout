import PlayerCard from '@/components/@composition/PlayerCard/PlayerCard';
import Divider from '@/components/@common/Divider/Divider';
import ProgressCounter from '@/components/@common/ProgressCounter/ProgressCounter';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import * as S from './ParticipantSection.styled';

export const ParticipantSection = () => {
  return (
    <>
      <SectionTitle
        title="참가자"
        description="음료 아이콘을 누르면 음료를 변경할 수 있습니다"
        suffix={<ProgressCounter current={7} total={9} />}
      />
      <PlayerCard name="홍길동" iconColor="red">
        아이콘
      </PlayerCard>
      <S.DividerWrapper>
        <Divider />
      </S.DividerWrapper>
      <S.ScrollableWrapper>
        {['다이앤', '니야', '메리', '루키', '한스', '꾹이', '엠제이', '1'].map((name) => (
          <PlayerCard key={name} name={name} iconColor="red">
            아이콘
          </PlayerCard>
        ))}
        <div style={{ height: '3rem' }}></div>
      </S.ScrollableWrapper>
    </>
  );
};
