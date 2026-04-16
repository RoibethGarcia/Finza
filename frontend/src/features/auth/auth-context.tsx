import { createContext, PropsWithChildren, useContext, useEffect, useMemo, useState } from 'react';
import { api, loadStoredSession, persistSession } from '../../shared/api/client';
import { AuthSession, UserProfile } from '../../shared/api/types';

type SessionState = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  accessTokenExpiresAt: string;
  refreshTokenExpiresAt: string;
  user: UserProfile;
};

type AuthContextValue = {
  session: SessionState | null;
  login: (credentials: { email: string; password: string }) => Promise<void>;
  register: (payload: { fullName: string; email: string; birthDate: string; password: string }) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function toSessionState(session: AuthSession): SessionState {
  return {
    accessToken: session.accessToken,
    refreshToken: session.refreshToken,
    tokenType: session.tokenType,
    accessTokenExpiresAt: session.accessTokenExpiresAt,
    refreshTokenExpiresAt: session.refreshTokenExpiresAt,
    user: session.user,
  };
}

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<SessionState | null>(() => loadStoredSession());

  useEffect(() => {
    persistSession(session);
  }, [session]);

  const value = useMemo<AuthContextValue>(() => ({
    session,
    async login(credentials) {
      const authSession = await api.login(credentials);
      setSession(toSessionState(authSession));
    },
    async register(payload) {
      const authSession = await api.register(payload);
      setSession(toSessionState(authSession));
    },
    async logout() {
      if (session) {
        await api.logout(session.refreshToken);
      }
      setSession(null);
    },
  }), [session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
