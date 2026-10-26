import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi, getStoredUser, getToken, clearAuthData } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(getStoredUser());
  const [token, setToken] = useState(getToken());
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Listen for global unauthorized events from Axios interceptor
    const handleUnauthorized = () => {
      clearAuthData();
      setUser(null);
      setToken(null);
    };

    window.addEventListener('careerforge:unauthorized', handleUnauthorized);
    return () => {
      window.removeEventListener('careerforge:unauthorized', handleUnauthorized);
    };
  }, []);

  const login = async (credentials) => {
    setLoading(true);
    try {
      const data = await authApi.login(credentials);
      const authUser = {
        userId: data.userId,
        name: data.name,
        email: data.email,
        role: data.role,
      };
      setUser(authUser);
      setToken(data.token);
      return data;
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const data = await authApi.register(userData);
      const authUser = {
        userId: data.userId,
        name: data.name,
        email: data.email,
        role: data.role,
      };
      setUser(authUser);
      setToken(data.token);
      return data;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    authApi.logout();
    setUser(null);
    setToken(null);
  };

  const isCandidate = user?.role === 'CANDIDATE';
  const isRecruiter = user?.role === 'RECRUITER';
  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isCandidate,
        isRecruiter,
        isAdmin,
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
