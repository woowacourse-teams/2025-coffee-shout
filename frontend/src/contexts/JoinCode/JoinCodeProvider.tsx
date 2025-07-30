import { PropsWithChildren, useState } from 'react';
import { JoinCodeContext } from './JoinCodeContext';

export const JoinCodeProvider = ({ children }: PropsWithChildren) => {
  const [joinCode, setJoinCodeState] = useState<string>('');

  const setJoinCode = (code: string) => setJoinCodeState(code);
  const clearJoinCode = () => setJoinCodeState('');

  return (
    <JoinCodeContext.Provider value={{ joinCode, setJoinCode, clearJoinCode }}>
      {children}
    </JoinCodeContext.Provider>
  );
};
