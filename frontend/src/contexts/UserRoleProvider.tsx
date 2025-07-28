import { UserRole } from '@/types/player';
import { UserRoleContext } from './UserRoleContext';
import { useState } from 'react';

export const UserRoleProvider = ({ children }: { children: React.ReactNode }) => {
  const [userRole, setUserRole] = useState<UserRole | null>(null);

  const setGuest = () => {
    setUserRole('GUEST');
  };

  const setHost = () => {
    setUserRole('HOST');
  };
  return (
    <UserRoleContext.Provider value={{ userRole, setGuest, setHost }}>
      {children}
    </UserRoleContext.Provider>
  );
};
