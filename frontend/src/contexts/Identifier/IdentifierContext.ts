import { createContext, useContext } from 'react';

type IdentifierContextType = {
  joinCode: string;
  setJoinCode: (joinCode: string) => void;
  clearJoinCode: () => void;

  myName: string;
  setMyName: (myName: string) => void;
  clearMyName: () => void;

  // TODO: player 정보와 함께 사용하는 것이 좋을듯
  menuId: number;
  setMenuId: (menuId: number) => void;
  clearMenuId: () => void;

  clearIdentifier: () => void;
};

export const IdentifierContext = createContext<IdentifierContextType | null>(null);

export const useIdentifier = () => {
  const context = useContext(IdentifierContext);
  if (!context) {
    throw new Error('useIdentifier 는 IdentifierProvider 안에서 사용해야 합니다.');
  }
  return context;
};
