import { ReactNode, useRef, useEffect } from 'react';
import * as S from './RacingGameOverlay.styled';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';

type Props = {
  children: ReactNode;
};

const RacingGameOverlay = ({ children }: Props) => {
  const { joinCode, myName } = useIdentifier();
  const { send } = useWebSocket();

  const tapCountRef = useRef(0);
  const intervalRef = useRef<number | null>(null);

  const handlePointerDown = () => {
    tapCountRef.current += 1;
  };

  useEffect(() => {
    intervalRef.current = window.setInterval(() => {
      send(`/room/${joinCode}/racing-game/tap`, {
        playerName: myName,
        tapCount: tapCountRef.current,
      });

      tapCountRef.current = 0;
    }, 200);
    return () => {
      if (intervalRef.current) {
        window.clearInterval(intervalRef.current);
      }
    };
  }, [joinCode, myName, send]);

  return <S.Overlay onPointerDown={handlePointerDown}>{children}</S.Overlay>;
};

export default RacingGameOverlay;
