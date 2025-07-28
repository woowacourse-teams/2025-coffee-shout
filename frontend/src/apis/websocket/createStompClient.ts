import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const createStompClient = () => {
  const client = new Client({
    webSocketFactory: () => new SockJS('http://api.coffee-shout.com/ws'),
    reconnectDelay: 5000,
    debug: (msg) => console.log('[STOMP]', msg),
  });

  client.onConnect = () => {
    console.log('✅ WebSocket 연결 성공');
  };

  client.onStompError = (frame) => {
    console.error('❌ STOMP error:', frame.headers['message']);
    console.error('Details:', frame.body);
  };

  return client;
};
