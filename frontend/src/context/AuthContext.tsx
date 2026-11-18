import React, { createContext, useContext, useState } from 'react';
import { api } from '../api/axiosClient';
import { AuthUser } from '../types';

interface AuthContextValue {
  user: AuthUser | null;
  login: (identifier: string, password: string) => Promise<void>;
  register: (payload: { username: string; email: string; password: string; fullName: string; phone: string }) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const saved = localStorage.getItem('bharatmart_auth');
    return saved ? JSON.parse(saved) : null;
  });

  const login = async (identifier: string, password: string) => {
    const res = await api.post('/auth/login', { identifier, password });
    const data: AuthUser = res.data.data;
    localStorage.setItem('bharatmart_auth', JSON.stringify(data));
    setUser(data);
  };

  const register = async (payload: { username: string; email: string; password: string; fullName: string; phone: string }) => {
    const res = await api.post('/auth/register', payload);
    const data: AuthUser = res.data.data;
    localStorage.setItem('bharatmart_auth', JSON.stringify(data));
    setUser(data);
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } finally {
      localStorage.removeItem('bharatmart_auth');
      setUser(null);
    }
  };

  const hasRole = (role: string) => Boolean(user?.roles.includes(role));

  return (
    <AuthContext.Provider value={{ user, login, register, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
};
