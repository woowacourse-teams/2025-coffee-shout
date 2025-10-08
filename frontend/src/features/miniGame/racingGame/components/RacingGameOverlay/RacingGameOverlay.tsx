import { ReactNode, MouseEvent, TouchEvent } from 'react';
import * as S from './RacingGameOverlay.styled';
import { useWebSocket } from '@/apis/websocket/contexts/WebSocketContext';
import { useIdentifier } from '@/contexts/Identifier/IdentifierContext';

type Props = {
  children: ReactNode;
};

const RacingGameOverlay = ({ children }: Props) => {
  const { joinCode, myName } = useIdentifier();
  const { send } = useWebSocket();
  const handleClick = (event: MouseEvent<HTMLDivElement> | TouchEvent<HTMLDivElement>) => {
    event.stopPropagation();
    console.log('clicked tabCount: 30');
    send(`/room/${joinCode}/racing-game/tap`, {
      playerName: myName,
      tabCount: 30,
    });
  };

  return <S.Overlay onClick={handleClick}>{children}</S.Overlay>;
};

export default RacingGameOverlay;
