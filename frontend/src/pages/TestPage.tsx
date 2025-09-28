import { Client, Message, Stomp } from '@stomp/stompjs';
import { useState, useRef, useEffect } from 'react';
import SockJS from 'sockjs-client';
import PlayerIcon from '@/components/@composition/PlayerIcon/PlayerIcon';
import { colorList } from '@/constants/color';
import Layout from '@/layouts/Layout';
import styled from '@emotion/styled';
import Headline4 from '@/components/@common/Headline4/Headline4';
import nightSkyImage from '@/assets/night-sky.png';
import Button from '@/components/@common/Button/Button';

type BallData = {
  x: number;
  y: number;
  width: number;
  height: number;
};

const TestPage = () => {
  const [isConnected, setIsConnected] = useState(false);
  const [ballDataList, setBallDataList] = useState<BallData[]>([
    { x: 0, y: 0, width: 60, height: 60 },
    { x: 0, y: 0, width: 60, height: 60 },
    { x: 0, y: 0, width: 60, height: 60 },
    { x: 0, y: 0, width: 60, height: 60 },
    { x: 0, y: 0, width: 60, height: 60 },
    { x: 0, y: 0, width: 60, height: 60 },
    { x: 0, y: 0, width: 60, height: 60 },
    { x: 0, y: 0, width: 60, height: 60 },
    { x: 0, y: 0, width: 60, height: 60 },
  ]);
  const [displayBallDataList, setDisplayBallDataList] = useState<BallData[]>([]);
  const [imageWidth, setImageWidth] = useState(1000); // 기본값
  const stompClientRef = useRef<Client | null>(null);
  const animationRef = useRef<number | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const pixelsPerSecond = 300; // 배경 이동 속도 (픽셀/초) - 3배 증가
  const backgroundPositionRef = useRef(0); // 배경 위치 추적

  const handleBallMessage = (message: Message) => {
    const dataList: BallData[] = JSON.parse(message.body);
    console.log('Received ball data:', dataList);
    setBallDataList(dataList);
  };

  // 배경 애니메이션 (독립적으로 실행)
  useEffect(() => {
    let lastTimestamp = performance.now();

    const animateBackground = (timestamp: number) => {
      const delta = timestamp - lastTimestamp;
      lastTimestamp = timestamp;

      // 시간 기반 배경 이동 (픽셀/초 단위로 속도 계산)
      backgroundPositionRef.current -= (pixelsPerSecond * delta) / 1000;

      // 배경 위치가 너무 커지면 반복 범위 내에서 순환
      if (backgroundPositionRef.current < -imageWidth) {
        backgroundPositionRef.current += imageWidth;
      }

      // React 상태 대신 DOM 직접 조작
      if (containerRef.current) {
        containerRef.current.style.backgroundPosition = `${backgroundPositionRef.current}px 0px`;
      }

      animationRef.current = requestAnimationFrame(animateBackground);
    };

    animationRef.current = requestAnimationFrame(animateBackground);

    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, [pixelsPerSecond, imageWidth]);

  // 플레이어 위치 계산 (ballDataList 변경 시에만)
  useEffect(() => {
    if (ballDataList.length > 0) {
      const adjustedBallDataList = ballDataList.map((ball, index) => {
        if (ballDataList.length > 5 && ballDataList[5]) {
          const centerBall = ballDataList[5]; // index 5가 기준점 (주인공)
          const screenCenter = { x: 200, y: window.innerHeight / 2 }; // 화면 중앙 좌표

          // index 5 기준으로 각 플레이어의 상대적 위치 계산
          const relativeX = ball.x - centerBall.x; // x축 상대적 거리
          const verticalOffset = (index - 5) * 80; // y축은 index 5 기준으로 80px 간격

          // 최종 화면 좌표 계산 (화면 범위 제한 제거)
          const finalX = screenCenter.x + relativeX; // 자연스럽게 화면 밖으로 나갈 수 있도록
          const finalY = screenCenter.y + verticalOffset; // 화면 중앙 + 세로 오프셋

          return {
            ...ball,
            x: finalX,
            y: finalY,
          };
        }
        return ball;
      });

      setDisplayBallDataList(adjustedBallDataList);
    }
  }, [ballDataList]);

  // 이미지 크기 측정
  useEffect(() => {
    const img = new Image();
    img.onload = () => {
      setImageWidth(img.naturalWidth);
    };
    img.src = nightSkyImage;
  }, []);

  const connect = () => {
    const socket = new SockJS('http://43.203.253.24:8080/ws');
    const stompClient = Stomp.over(socket);
    stompClientRef.current = stompClient;

    stompClient.connect({}, () => {
      setIsConnected(true);
      stompClient.subscribe('/topic/test', handleBallMessage);
    });
  };

  const disconnect = () => {
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
      setIsConnected(false);
      stompClientRef.current = null;
    }
  };

  return (
    <Layout color="gray-100">
      <Layout.TopBar center={<Headline4>Test</Headline4>} />
      <Layout.Content>
        <Container ref={containerRef}>
          <ButtonContainer>
            <Button
              onClick={connect}
              style={{
                opacity: isConnected ? 0.5 : 1,
                pointerEvents: isConnected ? 'none' : 'auto',
              }}
              height="small"
            >
              Connect
            </Button>
            <Button
              onClick={disconnect}
              style={{
                opacity: !isConnected ? 0.5 : 1,
                pointerEvents: !isConnected ? 'none' : 'auto',
              }}
              height="small"
            >
              Disconnect
            </Button>
          </ButtonContainer>
          {displayBallDataList.map((ball, index) => (
            <div
              key={index}
              style={{
                position: 'absolute',
                left: ball.x - ball.width / 2,
                top: ball.y - ball.height / 2,
                width: '50px',
                height: '50px',
                animation: 'rotate 300ms linear infinite',
                transformOrigin: 'center center', // 회전 중심을 중앙으로 설정
              }}
            >
              <PlayerIcon color={colorList[index % colorList.length]} />
            </div>
          ))}
        </Container>
      </Layout.Content>
    </Layout>
  );
};

export default TestPage;

const Container = styled.div`
  position: relative;
  background-image: url(${nightSkyImage});
  background-size: auto 100%;
  background-repeat: repeat-x;
  width: 100%;
  height: 100%;
  overflow: hidden;

  @keyframes rotate {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
`;

const ButtonContainer = styled.div`
  display: flex;
  flex-direction: row;
  gap: 16px;
`;
