import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem('ember_user');
    if (stored) setUser(JSON.parse(stored));
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const res = await authApi.login({ email, password });
    const { token, userId, name } = res.data;
    const userData = { id : userId, name, email };
    localStorage.setItem('ember_token', token);
    localStorage.setItem('ember_user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const register = async (name, email, password) => {
    const res = await authApi.register({ name, email, password });
    return res.data;
  };

  const logout = () => {
    localStorage.removeItem('ember_token');
    localStorage.removeItem('ember_user');
    setUser(null);
  };

  const deleteAccount = async () => {
    const token = localStorage.getItem('ember_token');
    const base = import.meta.env.VITE_API_URL || 'http://localhost:8081';
    const response = await fetch(`${base}/api/users/${user.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!response.ok) throw new Error('Failed to delete account');
    logout();
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, deleteAccount, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
