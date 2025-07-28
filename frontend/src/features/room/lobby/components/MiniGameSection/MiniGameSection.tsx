import GameActionButton from '@/components/@common/GameActionButton/GameActionButton';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import * as S from './MiniGameSection.styled';
import { useState } from 'react';

export const MiniGameSection = () => {
  const [selectedGame, setSelectedGame] = useState<number>(-1);

  const handleGameClick = (gameId: number) => {
    setSelectedGame(gameId);
  };

  return (
    <>
      <SectionTitle title="미니게임" description="미니게임을 선택해주세요" />
      <S.Wrapper>
        <GameActionButton
          onClick={() => handleGameClick(0)}
          isSelected={selectedGame === 0}
          gameName="카드게임"
        />
        <GameActionButton
          onClick={() => handleGameClick(1)}
          isSelected={selectedGame === 1}
          gameName="랜덤31"
        />
      </S.Wrapper>
    </>
  );
};
