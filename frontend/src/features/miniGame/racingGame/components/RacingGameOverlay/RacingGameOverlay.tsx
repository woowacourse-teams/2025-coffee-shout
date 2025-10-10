import { ReactNode, MouseEvent, TouchEvent, useRef, useEffect } from 'react';
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
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const handleClick = () => {
    tapCountRef.current += 1;
  };

  useEffect(() => {
    intervalRef.current = setInterval(() => {
      send(`/room/${joinCode}/racing-game/tap`, {
        playerName: myName,
        tapCount: tapCountRef.current,
      });

      tapCountRef.current = 0;
    }, 200);
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [joinCode, myName, send]);

  return <S.Overlay onClick={handleClick}>{children}</S.Overlay>;
};

export default RacingGameOverlay;
