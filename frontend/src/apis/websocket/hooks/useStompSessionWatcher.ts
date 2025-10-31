import { Client, IFrame } from '@stomp/stompjs';
import { useEffect, useRef, useState } from 'react';

export const useStompSessionWatcher = (client: Client | null, connectFrame?: IFrame | null) => {
  const [sessionId, setSessionId] = useState<string | null>(null);
  const prevSessionIdRef = useRef<string | null>(null);

  useEffect(() => {
    if (!client || !connectFrame) return;

    const currentSessionId = extractSessionId(client);

    if (currentSessionId && currentSessionId !== prevSessionIdRef.current) {
      console.log('🔄 SessionId 변경 감지', {
        prev: prevSessionIdRef.current,
        cur: currentSessionId,
      });
      setSessionId(currentSessionId);
      prevSessionIdRef.current = currentSessionId;
    }
  }, [client, connectFrame]);

  return { sessionId };
};

const extractSessionId = (stompClient: Client): string | null => {
  try {
    const ws = stompClient.webSocket as any;
    if (!ws) return null;
    if (ws._transport?.url) {
      const match = ws._transport.url.match(/\/([a-zA-Z0-9_-]+)\/[^/]+$/);
      if (match) return match[1];
    }
    if ('sessionId' in ws) {
      return ws.sessionId as string;
    }
    return null;
  } catch (err) {
    console.warn('⚠️ SessionId 추출 실패', err);
    return null;
  }
};
