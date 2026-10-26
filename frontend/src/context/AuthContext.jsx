import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi, getStoredUser, getToken, clearAuthData } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(getStoredUser());
  const [token, setToken] = useState(getToken());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Verify token validity on initial mount
    const verifyUser = async () => {
      const storedToken = getToken();
      if (!storedToken) {
        setLoading(false);
        return;
      }

      try {
        const currentUser = await authApi.me();
        setUser({
          userId: currentUser.userId || currentUser.id,
          name: currentUser.name,
          email: currentUser.email,
          role: currentUser.role,
        });
      } catch (err) {
        console.warn('Auth verification failed, clearing credentials', err.message);
        clearAuthData();
        setUser(null);
        setToken(null);
      } finally {
        setLoading(false);
      }
    };

    verifyUser();

    // Listen for global unauthorized events from api.js
    const handleUnauthorized = () => {
      setUser(null);
      setToken(null);
    };
    window.addEventListener('auth:unauthorized', handleUnauthorized);

    return () => {
      window.removeEventListener('auth:unauthorized', handleUnauthorized);
    };
  }, []);

  const login = async (credentials) => {
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
  };

  const register = async (userData) => {
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
