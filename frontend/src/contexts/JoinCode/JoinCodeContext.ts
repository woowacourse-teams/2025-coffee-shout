import { createContext, useContext } from 'react';

type JoinCodeContextType = {
  joinCode: string | null;
  setJoinCode: (code: string) => void;
  clearJoinCode: () => void;
};

export const JoinCodeContext = createContext<JoinCodeContextType | null>(null);

export const useJoinCode = () => {
  const context = useContext(JoinCodeContext);
  if (!context) {
    throw new Error('useJoinCode 는 JoinCodeProvider 안에서 사용해야 합니다.');
  }
  return context;
};
