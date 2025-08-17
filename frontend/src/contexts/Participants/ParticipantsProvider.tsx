import { PropsWithChildren, useState } from 'react';
import { ParticipantsContext } from './ParticipantsContext';
import { Player } from '@/types/player';

export const ParticipantsProvider = ({ children }: PropsWithChildren) => {
  const [participants, setParticipants] = useState<Player[]>([]);
  const isAllReady = participants.every((participant) => participant.isReady);

  return (
    <ParticipantsContext.Provider
      value={{
        participants,
        setParticipants,
        isAllReady,
      }}
    >
      {children}
    </ParticipantsContext.Provider>
  );
};
