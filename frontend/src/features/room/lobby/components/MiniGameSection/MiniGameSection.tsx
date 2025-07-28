import { api } from '@/apis/api';
import { ApiError, NetworkError } from '@/apis/error';
import GameActionButton from '@/components/@common/GameActionButton/GameActionButton';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { useEffect, useState } from 'react';
import * as S from './MiniGameSection.styled';

const API_URL = process.env.REACT_APP_API_URL;

const MINI_GAME_NAME_MAP = {
  CARD_GAME: '카드게임',
  '31_GAME': '랜덤 31',
} as const;

type MiniGameType = keyof typeof MINI_GAME_NAME_MAP;

type MiniGamesResponse = {
  miniGameType: MiniGameType;
}[];

export const MiniGameSection = () => {
  const [selectedMiniGame, setSelectedMiniGame] = useState<string | null>(null);
  const [miniGames, setMiniGames] = useState<MiniGameType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const handleMiniGameClick = (miniGameType: string) => {
    setSelectedMiniGame(miniGameType);
  };

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const _miniGames = await api.get<MiniGamesResponse>(`${API_URL}/rooms/minigames`);
        setMiniGames(_miniGames.map((game) => game.miniGameType));
      } catch (error) {
        if (error instanceof ApiError) {
          setError(error.message);
        } else if (error instanceof NetworkError) {
          setError('네트워크 연결을 확인해주세요');
        } else {
          setError('알 수 없는 오류가 발생했습니다');
        }
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <>
      <SectionTitle title="미니게임" description="미니게임을 선택해주세요" />
      <S.Wrapper>
        {loading && <div>로딩 중...</div>}
        {!loading && error && <div>{error}</div>}
        {!loading &&
          !error &&
          miniGames.map((miniGame) => (
            <GameActionButton
              key={miniGame}
              isSelected={selectedMiniGame === miniGame}
              gameName={MINI_GAME_NAME_MAP[miniGame]}
              onClick={() => handleMiniGameClick(miniGame)}
            />
          ))}
      </S.Wrapper>
    </>
  );
};
