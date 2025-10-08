import { PropsWithChildren, useCallback, useState } from 'react';
import { RacingGameContext } from './RacingGameContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { RacingGameState } from '@/types/miniGame/racingGame';
import { useIdentifier } from '../Identifier/IdentifierContext';

const RacingGameProvider = ({ children }: PropsWithChildren) => {
  const [racingGameState, setRacingGameState] = useState<RacingGameState>('DESCRIPTION');
  const { joinCode } = useIdentifier();

  const handleRacingGameState = useCallback((data: { state: RacingGameState }) => {
    console.log('handleRacingGameState', data.state);
    setRacingGameState(data.state);
  }, []);

  useWebSocketSubscription(`/room/${joinCode}/racing-game/state`, handleRacingGameState);

  return (
    <RacingGameContext.Provider value={{ racingGameState }}>{children}</RacingGameContext.Provider>
  );
};

export default RacingGameProvider;
