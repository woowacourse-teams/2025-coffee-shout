import { useState } from 'react';
import { api } from '../api';
import { ApiError, NetworkError } from '../error';

export type User = {
  id: number;
  name: string;
  email: string;
  createdAt: string;
};

export type CreateUserRequest = {
  name: string;
  email: string;
};

const useUsers = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchUsers = async (params?: Record<string, any>) => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.get<User[]>('/api/users', { params });
      setUsers(response);
    } catch (error) {
      if (error instanceof ApiError) {
        setError(error.message);
      } else if (error instanceof NetworkError) {
        setError('네트워크 연결을 확인해주세요');
      } else {
        setError('알 수 없는 오류가 발생했습니다');
      }
    } finally {
      setLoading(false);
    }
  };

  const createUser = async (userData: CreateUserRequest) => {
    const newUser = await api.post<User, CreateUserRequest>('/api/users', userData);
    setUsers((prev) => [...prev, newUser]);
    return newUser;
  };

  return { users, loading, error, fetchUsers, createUser };
};

export default useUsers;
