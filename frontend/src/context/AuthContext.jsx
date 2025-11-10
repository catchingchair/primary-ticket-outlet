import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { apiRequest } from '../api/client';

const AuthContext = createContext(undefined);

const STORAGE_KEY = 'ticket-auth';

export function AuthProvider({ children }) {
  const [authState, setAuthState] = useState(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return { token: null, user: null, roles: [] };
    try {
      const parsed = JSON.parse(raw);
      return parsed;
    } catch {
      return { token: null, user: null, roles: [] };
    }
  });
  const [error, setError] = useState(null);
  const { token } = authState;

  useEffect(() => {
    if (!token) {
      if (authState.user !== null || authState.roles.length > 0) {
        setAuthState((prev) => ({ ...prev, user: null, roles: [] }));
      }
      localStorage.removeItem(STORAGE_KEY);
      return;
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(authState));
  }, [token, authState.user, authState.roles, authState.token]);

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    async function loadProfile() {
      try {
        const profile = await apiRequest('/me', {}, token);
        if (!cancelled) {
          setAuthState((prev) => ({
            ...prev,
            user: profile,
            roles: profile?.roles ?? [],
          }));
          setError(null);
        }
      } catch (err) {
        console.error(err);
        if (!cancelled) {
          setError('Failed to fetch profile');
        }
      }
    }
    loadProfile();
    return () => {
      cancelled = true;
    };
  }, [token]);

  const login = (authResponse) => {
    setAuthState({
      token: authResponse.token,
      user: {
        id: authResponse.userId,
        email: authResponse.email,
        displayName: authResponse.displayName,
        roles: authResponse.roles ?? [],
        managedVenues: [],
      },
      roles: authResponse.roles ?? [],
    });
    setError(null);
  };

  const logout = () => {
    setAuthState({ token: null, user: null, roles: [] });
    setError(null);
    localStorage.removeItem(STORAGE_KEY);
  };

  const value = useMemo(
    () => ({
      token,
      user: authState.user,
      roles: authState.roles,
      login,
      logout,
      error,
      setError,
      setUser: (user) =>
        setAuthState((prev) => ({
          ...prev,
          user,
          roles: user?.roles ?? prev.roles,
        })),
    }),
    [token, authState.user, authState.roles, error]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
