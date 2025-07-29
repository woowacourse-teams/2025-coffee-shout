import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const createStompClient = () => {
  const wsUrl = getWebSocketUrl();

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  return client;
};

const getWebSocketUrl = (): string => {
  const apiUrl = process.env.REACT_APP_API_URL;
  if (!apiUrl) {
    throw new Error('REACT_APP_API_URL is not defined');
  }

  return `${apiUrl}/ws`;
};
