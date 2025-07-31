import { CardGameContext } from './CardGameContext';
import { PropsWithChildren } from 'react';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { useIdentifier } from '../Identifier/IdentifierContext';

export const CardGameProvider = ({ children }: PropsWithChildren) => {
  const { joinCode } = useIdentifier();
  useWebSocketSubscription(`/room/${joinCode}/gameState`, (data: any) => {
    console.log(data);
  });
  return <CardGameContext.Provider value={{}}>{children}</CardGameContext.Provider>;
};
