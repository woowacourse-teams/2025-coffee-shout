import { api } from '@/apis/rest/api';
import { ApiError, NetworkError } from '@/apis/rest/error';
import CardIcon from '@/assets/card-icon.svg';
import GameActionButton from '@/components/@common/GameActionButton/GameActionButton';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import { MINI_GAME_DESCRIPTION, MINI_GAME_NAME_MAP, MiniGameType } from '@/types/miniGame';
import { useEffect, useState } from 'react';
import * as S from './MiniGameSection.styled';

type Props = {
  selectedMiniGames: MiniGameType[];
  handleMiniGameClick: (miniGameType: MiniGameType) => void;
};

export const MiniGameSection = ({ selectedMiniGames, handleMiniGameClick }: Props) => {
  const [miniGames, setMiniGames] = useState<MiniGameType[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { playerType } = usePlayerType();

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const _miniGames = await api.get<MiniGameType[]>('/rooms/minigames');
        setMiniGames(_miniGames);
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

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;

  return (
    <>
      <SectionTitle title="미니게임" description="미니게임을 선택해주세요" />
      <S.Wrapper>
        {miniGames.map((miniGame) => (
          <GameActionButton
            key={miniGame}
            isSelected={selectedMiniGames.includes(miniGame)}
            isDisabled={playerType === 'GUEST'}
            gameName={MINI_GAME_NAME_MAP[miniGame]}
            description={MINI_GAME_DESCRIPTION[miniGame]}
            onClick={() => handleMiniGameClick(miniGame)}
            icon={<S.Icon src={CardIcon} alt={miniGame} />}
          />
        ))}
      </S.Wrapper>
    </>
  );
};
