import CarouselSlide from '@/components/@common/CarouselSlide/CarouselSlide';
import RankingItem from '@/components/@common/RankingItem/RankingItem';
import * as S from './Top3WinnersSlide.styled';

type Winner = {
  name: string;
  count: number;
};

type Props = {
  winners: Winner[];
};

const Top3WinnersSlide = ({ winners }: Props) => {
  return (
    <CarouselSlide title="이번달 TOP3 당첨자">
      <S.Wrapper>
        {winners.slice(0, 3).map((winner, index) => (
          <RankingItem key={winner.name} rank={index + 1} name={winner.name} count={winner.count} />
        ))}
      </S.Wrapper>
    </CarouselSlide>
  );
};

export default Top3WinnersSlide;
