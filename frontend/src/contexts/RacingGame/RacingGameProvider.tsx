import { PropsWithChildren, useCallback, useState } from 'react';
import { RacingGameContext } from './RacingGameContext';
import { useWebSocketSubscription } from '@/apis/websocket/hooks/useWebSocketSubscription';
import { RacingGameState } from '@/types/miniGame/racingGame';
import { useIdentifier } from '../Identifier/IdentifierContext';

type RacingGameData = {
  distance: {
    start: number;
    end: number;
  };
  players: Array<{
    playerName: string;
    position: number; // 서버에서 position으로 보내고 있음
    speed: number;
  }>;
};

const RacingGameProvider = ({ children }: PropsWithChildren) => {
  const [racingGameState, setRacingGameState] = useState<RacingGameState>('DESCRIPTION');
  const [racingGameData, setRacingGameData] = useState<RacingGameData>({
    players: [],
    distance: {
      start: 0,
      end: 1000,
    },
  });
  const { joinCode } = useIdentifier();

  const handleRacingGameState = useCallback((data: { state: RacingGameState }) => {
    console.log('handleRacingGameState', data.state);
    setRacingGameState(data.state);
  }, []);
  const handleRacingGameData = useCallback((data: RacingGameData) => {
    setRacingGameData(data);
  }, []);

  useWebSocketSubscription(`/room/${joinCode}/racing-game/state`, handleRacingGameState);

  useWebSocketSubscription(`/room/${joinCode}/racing-game`, handleRacingGameData);

  return (
    <RacingGameContext.Provider value={{ racingGameState, racingGameData }}>
      {children}
    </RacingGameContext.Provider>
  );
};

export default RacingGameProvider;
