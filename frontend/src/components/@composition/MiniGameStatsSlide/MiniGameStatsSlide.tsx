import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import RankingItem from '@/components/@common/RankingItem/RankingItem';
import * as S from './MiniGameStatsSlide.styled';

type Game = {
  name: string;
  count: number;
};

type Props = {
  games: Game[];
};

const MiniGameStatsSlide = ({ games }: Props) => {
  return (
    <CarouselSlide title="미니게임 플레이 횟수">
      <S.Wrapper>
        {games.map((game, index) => (
          <RankingItem key={game.name} rank={index + 1} name={game.name} count={game.count} />
        ))}
      </S.Wrapper>
    </CarouselSlide>
  );
};

export default MiniGameStatsSlide;
