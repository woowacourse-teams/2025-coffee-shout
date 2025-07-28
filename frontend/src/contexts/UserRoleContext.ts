import { createContext, useContext } from 'react';
import { UserRole } from '@/types/player';

type UserRoleContextType = {
  userRole: UserRole | null;
  setGuest: () => void;
  setHost: () => void;
};

export const UserRoleContext = createContext<UserRoleContextType | null>(null);

export const useUserRole = () => {
  const context = useContext(UserRoleContext);
  if (!context) {
    throw new Error('useUserRole 는 UserRoleProvider 안에서 사용해야 합니다.');
  }
  return context;
};
