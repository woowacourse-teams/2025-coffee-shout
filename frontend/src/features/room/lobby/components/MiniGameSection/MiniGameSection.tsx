import useFetch from '@/apis/rest/useFetch';
import CardIcon from '@/assets/card-icon.svg';
import GameActionButton from '@/components/@common/GameActionButton/GameActionButton';
import SectionTitle from '@/components/@composition/SectionTitle/SectionTitle';
import { usePlayerType } from '@/contexts/PlayerType/PlayerTypeContext';
import {
  MINI_GAME_DESCRIPTION_MAP,
  MINI_GAME_NAME_MAP,
  MiniGameType,
} from '@/types/miniGame/common';
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

  if (loading) return <div>로딩 중...</div>;

  return (
    <>
      <SectionTitle title="미니게임" description="미니게임을 선택해주세요" />
      <S.Wrapper>
        {miniGames?.map((miniGame) => (
          <GameActionButton
            key={miniGame}
            isSelected={selectedMiniGames.includes(miniGame)}
            isDisabled={playerType === 'GUEST'}
            gameName={MINI_GAME_NAME_MAP[miniGame]}
            description={MINI_GAME_DESCRIPTION_MAP[miniGame]}
            onClick={() => handleMiniGameClick(miniGame)}
            icon={<S.Icon src={CardIcon} alt={miniGame} />}
          />
        ))}
      </S.Wrapper>
    </>
  );
};
