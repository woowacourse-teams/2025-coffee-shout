import { JoinCodeContext } from './JoinCodeContext';
import { PropsWithChildren, useState } from 'react';

export const JoinCodeProvider = ({ children }: PropsWithChildren) => {
  const [joinCode, setJoinCodeState] = useState<string | null>(null);

  const setJoinCode = (code: string) => {
    setJoinCodeState(code);
  };

  const clearJoinCode = () => {
    setJoinCodeState(null);
  };

  return (
    <JoinCodeContext.Provider value={{ joinCode, setJoinCode, clearJoinCode }}>
      {children}
    </JoinCodeContext.Provider>
  );
};
