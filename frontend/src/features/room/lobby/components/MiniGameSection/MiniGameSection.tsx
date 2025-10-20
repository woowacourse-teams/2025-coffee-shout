import useFetch from '@/apis/rest/useFetch';
import GameActionButton from '@/components/@common/GameActionButton/GameActionButton';
import ScreenReaderOnly from '@/components/@common/ScreenReaderOnly/ScreenReaderOnly';
import GameActionButtonSkeleton from '@/components/@composition/GameActionButtonSkeleton/GameActionButtonSkeleton';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import {
  MINI_GAME_DESCRIPTION_MAP,
  MINI_GAME_ICON_MAP,
  MINI_GAME_NAME_MAP,
  MiniGameType,
} from '@/types/miniGame/common';
import { useEffect, useRef, useState } from 'react';
import * as S from './MiniGameSection.styled';

type Props = {
  selectedMiniGames: MiniGameType[];
  handleMiniGameClick: (miniGameType: MiniGameType) => void;
};

export const MiniGameSection = ({ selectedMiniGames, handleMiniGameClick }: Props) => {
  const { playerType } = usePlayerType();
  const { data: miniGames, loading } = useFetch<MiniGameType[]>({
    endpoint: '/rooms/minigames',
  });
  const [screenReaderMessage, setScreenReaderMessage] = useState<string>('');
  const screenReaderRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!loading && miniGames) {
      const initialMessage =
        '미니게임 선택 영역입니다. 원하는 미니게임을 여러 개 선택할 수 있습니다.';
      setScreenReaderMessage(initialMessage);
    }
  }, [loading, miniGames]);

  useEffect(() => {
    if (screenReaderMessage && screenReaderRef.current) {
      screenReaderRef.current.focus();
    }
  }, [screenReaderMessage]);

  return (
    <>
      {screenReaderMessage && (
        <ScreenReaderOnly aria-live="assertive" ref={screenReaderRef}>
          {screenReaderMessage}
        </ScreenReaderOnly>
      )}
      <S.Wrapper>
        {loading ? (
          <GameActionButtonSkeleton />
        ) : (
          miniGames?.map((miniGame) => (
            <GameActionButton
              key={miniGame}
              isSelected={selectedMiniGames.includes(miniGame)}
              isDisabled={playerType === 'GUEST'}
              gameName={MINI_GAME_NAME_MAP[miniGame]}
              description={MINI_GAME_DESCRIPTION_MAP[miniGame]}
              onClick={() => handleMiniGameClick(miniGame)}
              icon={<S.Icon src={MINI_GAME_ICON_MAP[miniGame]} alt={miniGame} />}
            />
          ))
        )}
      </S.Wrapper>
    </>
  );
};
