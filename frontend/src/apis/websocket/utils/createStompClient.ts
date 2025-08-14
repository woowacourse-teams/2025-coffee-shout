import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getWebSocketUrl } from './getWebSocketUrl';

type Props = {
  joinCode: string;
  playerName: string;
  menuId: number;
};

export const createStompClient = ({ joinCode, playerName, menuId }: Props) => {
  const wsUrl = getWebSocketUrl();

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    debug: (msg) => console.log('[STOMP]', msg),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    connectHeaders: { joinCode, playerName, menuId: menuId.toString() },
  });

  return client;
};
