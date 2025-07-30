import { PropsWithChildren, useState } from 'react';
import { IdentifierContext } from './IdentifierContext';

export const IdentifierProvider = ({ children }: PropsWithChildren) => {
  const [joinCode, setJoinCode] = useState<string>('');
  const [myName, setMyName] = useState<string>('');

  const clearJoinCode = () => setJoinCode('');

  return (
    <IdentifierContext.Provider value={{ joinCode, setJoinCode, clearJoinCode, myName, setMyName }}>
      {children}
    </IdentifierContext.Provider>
  );
};
