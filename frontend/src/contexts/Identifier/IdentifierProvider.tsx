import { PropsWithChildren, useCallback, useState } from 'react';
import { IdentifierContext } from './IdentifierContext';

export const IdentifierProvider = ({ children }: PropsWithChildren) => {
  const [joinCode, setJoinCode] = useState<string>('');
  const [myName, setMyName] = useState<string>('');

  const clearJoinCode = useCallback(() => {
    setJoinCode('');
  }, []);

  const clearMyName = useCallback(() => {
    setMyName('');
  }, []);

  const clearIdentifier = useCallback(() => {
    clearJoinCode();
    clearMyName();
  }, [clearJoinCode, clearMyName]);

  return (
    <IdentifierContext.Provider
      value={{
        joinCode,
        setJoinCode,
        clearJoinCode,
        myName,
        setMyName,
        clearMyName,
        clearIdentifier,
      }}
    >
      {children}
    </IdentifierContext.Provider>
  );
};
