import Headline4 from '@/components/@common/Headline4/Headline4';
import styled from '@emotion/styled';
import { LAYOUT_PADDING } from '@/constants/padding';
import PlayerIcon from '@/components/@composition/PlayerIcon/PlayerIcon';
import { colorList } from '@/constants/color';
import skyImage from '@/assets/sky.png';

const mockData = [
  { playerName: '홍길동', x: '100px', speed: 10 },
  { playerName: '김철수', x: '0px', speed: 10 },
  { playerName: '이순신', x: '300px', speed: 20 },
  { playerName: '박영희', x: '0px', speed: 20 },
  { playerName: '정민수', x: '100px', speed: 40 },
  { playerName: '최지영', x: '40px', speed: 60 },
  { playerName: '강동원', x: '50px', speed: 30 },
  { playerName: '윤서연', x: '180px', speed: 10 },
  { playerName: '임태현', x: '0px', speed: 10 },
];

const myName = '정민수';
const myX = '100px';
const mySpeed = 40;

const RacingGamePage = () => {
  return (
    <Container $speed={mySpeed}>
      <HeadlineWrapper>
        <Headline4>레이싱 게임</Headline4>
      </HeadlineWrapper>
      <ContentWrapper>
        <PlayersWrapper>
          {mockData.map((item, index) => (
            <Player key={item.playerName} $isMe={item.playerName === myName} $x={item.x} $myX={myX}>
              <RotatingWrapper $speed={item.speed}>
                <PlayerIcon color={colorList[index % colorList.length]} />
              </RotatingWrapper>
            </Player>
          ))}
        </PlayersWrapper>
      </ContentWrapper>
    </Container>
  );
};

export default RacingGamePage;

const Container = styled.div<{ $speed: number }>`
  width: 100%;
  height: 100%;
  padding: ${LAYOUT_PADDING} 0;
  background-image: url(${skyImage});
  background-size: cover;
  background-position: center;
  background-repeat: repeat-x;
  display: flex;
  flex-direction: column;
  animation: moveBackground ${({ $speed }) => 40 / $speed}s linear infinite;

  @keyframes moveBackground {
    from {
      background-position: 0% center;
    }
    to {
      background-position: 100% center;
    }
  }
`;

const HeadlineWrapper = styled.div`
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 2.6rem;
`;

const ContentWrapper = styled.div`
  width: 100%;

  flex: 1;
  overflow: hidden;
`;

const PlayersWrapper = styled.div`
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 3rem;
  justify-content: center;
  align-items: center;
`;

const Player = styled.div<{ $isMe: boolean; $x: string; $myX: string }>`
  transform: ${({ $isMe, $x, $myX }) => {
    if ($isMe) return 'translateX(0)';
    const xValue = parseInt($x.replace('px', ''));
    const relativeX = xValue - parseInt($myX.replace('px', ''));
    return `translateX(${relativeX}px)`;
  }};
  transition: transform 0.3s ease-in-out;
`;

//회전 담당하는 래퍼
const RotatingWrapper = styled.div<{ $speed: number }>`
  animation: spin ${({ $speed }) => 10 / $speed}s linear infinite;

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
`;
