import { ReactNode, useRef, useEffect } from 'react';
import * as S from './RacingGameOverlay.styled';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';
import { useRacingGame } from '@/contexts/RacingGame/RacingGameContext';

type Props = {
  children: ReactNode;
};

const RacingGameOverlay = ({ children }: Props) => {
  const { racingGameState } = useRacingGame();
  const { joinCode, myName } = useIdentifier();
  const { send } = useWebSocket();

  const tapCountRef = useRef(0);
  const intervalRef = useRef<number | null>(null);

  const handlePointerDown = () => {
    tapCountRef.current += 1;
  };

  useEffect(() => {
    intervalRef.current = window.setInterval(() => {
      if (racingGameState !== 'PLAYING') return;
      const currentTapCount = tapCountRef.current;
      tapCountRef.current = 0;

      send(`/room/${joinCode}/racing-game/tap`, {
        playerName: myName,
        tapCount: currentTapCount,
      });
    }, 200);
    return () => {
      if (intervalRef.current) {
        window.clearInterval(intervalRef.current);
      }
    };
  }, [joinCode, myName, send, racingGameState]);

  return <S.Overlay onPointerDown={handlePointerDown}>{children}</S.Overlay>;
};

export default RacingGameOverlay;
