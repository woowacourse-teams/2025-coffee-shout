import GameActionButton from '@/components/@common/GameActionButton/GameActionButton';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import * as S from './MiniGameSection.styled';

export const MiniGameSection = () => {
  return (
    <>
      <SectionTitle title="미니게임" description="미니게임을 선택해주세요" />
      <S.Wrapper>
        <GameActionButton onClick={() => {}} isSelected={true} gameName="카드게임" />
        <GameActionButton onClick={() => {}} isSelected={false} gameName="랜덤31" />
      </S.Wrapper>
    </>
  );
};
