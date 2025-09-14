import { PropsWithChildren, useCallback, useEffect, useState } from 'react';
import { IdentifierContext } from './IdentifierContext';

const STORAGE_KEYS = {
  JOIN_CODE: 'coffee-shout-join-code',
  MY_NAME: 'coffee-shout-my-name',
} as const;

export const IdentifierProvider = ({ children }: PropsWithChildren) => {
  const [joinCode, setJoinCode] = useState<string>(() => {
    return sessionStorage.getItem(STORAGE_KEYS.JOIN_CODE) || '';
  });
  const [myName, setMyName] = useState<string>(() => {
    return sessionStorage.getItem(STORAGE_KEYS.MY_NAME) || '';
  });

  useEffect(() => {
    if (joinCode) {
      sessionStorage.setItem(STORAGE_KEYS.JOIN_CODE, joinCode);
    } else {
      sessionStorage.removeItem(STORAGE_KEYS.JOIN_CODE);
    }
  }, [joinCode]);

  useEffect(() => {
    if (myName) {
      sessionStorage.setItem(STORAGE_KEYS.MY_NAME, myName);
    } else {
      sessionStorage.removeItem(STORAGE_KEYS.MY_NAME);
    }
  }, [myName]);

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
