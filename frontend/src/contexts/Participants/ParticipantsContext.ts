import { createContext, useContext } from 'react';
import { Player } from '@/types/player';

type ParticipantsContextType = {
  participants: Player[];
  setParticipants: (participants: Player[]) => void;
  isAllReady: boolean;
  getParticipantColorIndex: (playerName: string) => number;
};

export const ParticipantsContext = createContext<ParticipantsContextType | null>(null);

export const useParticipants = () => {
  const context = useContext(ParticipantsContext);
  if (!context) {
    throw new Error('useParticipants 는 ParticipantsProvider 안에서 사용해야 합니다.');
  }
  return context;
};
