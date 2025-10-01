import Headline4 from '@/components/@common/Headline4/Headline4';
import RacingPlayer from './components/RacingPlayer/RacingPlayer';
import * as S from './RacingGamePage.styled';

const mockData = [
  { playerName: '홍길동', x: 100, speed: 10 },
  { playerName: '김철수', x: 0, speed: 10 },
  { playerName: '이순신', x: 300, speed: 20 },
  { playerName: '박영희', x: 0, speed: 20 },
  { playerName: '정민수', x: 100, speed: 40 },
  { playerName: '최지영', x: 40, speed: 60 },
  { playerName: '강동원', x: 50, speed: 30 },
  { playerName: '윤서연', x: 180, speed: 10 },
  { playerName: '임태현', x: 0, speed: 10 },
];

const myName = '정민수';
const myX = 100;
const mySpeed = 40;

const RacingGamePage = () => {
  return (
    <S.Container $speed={mySpeed}>
      <S.HeadlineWrapper>
        <Headline4>레이싱 게임</Headline4>
      </S.HeadlineWrapper>
      <S.ContentWrapper>
        <S.PlayersWrapper>
          {mockData.map((item, index) => (
            <RacingPlayer
              key={item.playerName}
              playerName={item.playerName}
              x={item.x}
              speed={item.speed}
              isMe={item.playerName === myName}
              myX={myX}
              colorIndex={index}
            />
          ))}
        </S.PlayersWrapper>
      </S.ContentWrapper>
    </S.Container>
  );
};

export default RacingGamePage;
