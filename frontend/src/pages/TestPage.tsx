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
  const [ballDataList, setBallDataList] = useState<BallData[]>([]);
  const [displayBallDataList, setDisplayBallDataList] = useState<BallData[]>([]);
  const [backgroundOffset, setBackgroundOffset] = useState({ x: 0, y: 0 });
  const [imageWidth, setImageWidth] = useState(1000); // 기본값
  const stompClientRef = useRef<Client | null>(null);
  const animationRef = useRef<number | null>(null);

  const handleBallMessage = (message: Message) => {
    const dataList: BallData[] = JSON.parse(message.body);
    console.log('Received ball data:', dataList);
    setBallDataList(dataList);
  };

  useEffect(() => {
    const animate = () => {
      setDisplayBallDataList(ballDataList);
      animationRef.current = requestAnimationFrame(animate);
    };

    if (ballDataList.length > 0) {
      animationRef.current = requestAnimationFrame(animate);
    }

    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, [ballDataList]);

  // 이미지 크기 측정
  useEffect(() => {
    const img = new Image();
    img.onload = () => {
      setImageWidth(img.naturalWidth);
    };
    img.src = nightSkyImage;
  }, []);

  useEffect(() => {
    // 첫 번째 ball의 위치를 기준으로 배경 오프셋 계산 (x축만, 무한 반복)
    if (ballDataList.length > 0) {
      const firstBall = ballDataList[0];
      const offsetX = -((firstBall.x - 200) % imageWidth);
      setBackgroundOffset({
        x: offsetX,
        y: 0, // y축 이동 없음
      });
    }
  }, [ballDataList, imageWidth]);

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
        <Container offsetX={backgroundOffset.x} offsetY={backgroundOffset.y}>
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
          <div
            style={{
              position: 'absolute',
              left: '50%',
              top: '50%',
              transform: 'translate(-50%, -50%)',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '60px',
            }}
          >
            {displayBallDataList.map((ball, index) => (
              <div
                key={index}
                style={{
                  width: ball.width,
                  height: ball.height,
                }}
              >
                <PlayerIcon color={colorList[index % colorList.length]} />
              </div>
            ))}
          </div>
        </Container>
      </Layout.Content>
    </Layout>
  );
};

export default TestPage;

const Container = styled.div<{ offsetX: number; offsetY: number }>`
  position: relative;
  background-image: url(${nightSkyImage});
  background-size: auto 100%;
  background-position: ${({ offsetX, offsetY }) => `${offsetX}px ${offsetY}px`};
  background-repeat: repeat-x;
  width: 100%;
  height: 100%;
  overflow: hidden;
  transition: background-position 0.05s linear;
`;

const ButtonContainer = styled.div`
  display: flex;
  flex-direction: row;
  gap: 16px;
`;
