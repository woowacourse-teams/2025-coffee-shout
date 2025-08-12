import { PropsWithChildren, useCallback, useState } from 'react';
import { IdentifierContext } from './IdentifierContext';

export const IdentifierProvider = ({ children }: PropsWithChildren) => {
  const [joinCode, setJoinCode] = useState<string>('');
  const [myName, setMyName] = useState<string>('');
  const [menuId, setMenuId] = useState<number>(-1);

  const clearJoinCode = useCallback(() => {
    setJoinCode('');
  }, []);

  const clearMyName = useCallback(() => {
    setMyName('');
  }, []);

  const clearMenuId = useCallback(() => {
    setMenuId(-1);
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
        menuId,
        setMenuId,
        clearMenuId,
      }}
    >
      {children}
    </IdentifierContext.Provider>
  );
};
