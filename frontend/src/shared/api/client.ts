import { AuthSession, Category, DashboardSummary, Transaction, UserProfile, Account } from './types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
const SESSION_STORAGE_KEY = 'finza.session';

type SessionPayload = Pick<AuthSession, 'accessToken' | 'refreshToken' | 'user' | 'tokenType' | 'accessTokenExpiresAt' | 'refreshTokenExpiresAt'>;

export function loadStoredSession(): SessionPayload | null {
  const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
  return raw ? JSON.parse(raw) as SessionPayload : null;
}

export function persistSession(session: SessionPayload | null) {
  if (!session) {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return;
  }
  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
}

async function request<T>(path: string, init?: RequestInit, accessToken?: string): Promise<T> {
  const headers = new Headers(init?.headers ?? {});
  headers.set('Content-Type', 'application/json');
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.detail ?? body?.message ?? 'Request failed');
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  register: (payload: { fullName: string; email: string; birthDate: string; password: string }) =>
    request<AuthSession>('/api/v1/auth/register', { method: 'POST', body: JSON.stringify(payload) }),
  login: (payload: { email: string; password: string }) =>
    request<AuthSession>('/api/v1/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  logout: (refreshToken: string) =>
    request<void>('/api/v1/auth/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) }),
  me: (accessToken: string) => request<UserProfile>('/api/v1/auth/me', { method: 'GET' }, accessToken),
  getDashboard: (accessToken: string) => request<DashboardSummary>('/api/v1/dashboard', { method: 'GET' }, accessToken),
  listAccounts: (accessToken: string) => request<Account[]>('/api/v1/accounts', { method: 'GET' }, accessToken),
  createAccount: (accessToken: string, payload: { name: string; type: string; currency: string; openingBalance: number }) =>
    request<Account>('/api/v1/accounts', { method: 'POST', body: JSON.stringify(payload) }, accessToken),
  listCategories: (accessToken: string) => request<Category[]>('/api/v1/categories', { method: 'GET' }, accessToken),
  createCategory: (accessToken: string, payload: { name: string; type: string }) =>
    request<Category>('/api/v1/categories', { method: 'POST', body: JSON.stringify(payload) }, accessToken),
  listTransactions: (accessToken: string) => request<Transaction[]>('/api/v1/transactions', { method: 'GET' }, accessToken),
  createTransaction: (accessToken: string, payload: { accountId: string; categoryId: string; type: string; amount: number; occurredAt: string; description?: string }) =>
    request<Transaction>('/api/v1/transactions', { method: 'POST', body: JSON.stringify(payload) }, accessToken),
  createTransfer: (accessToken: string, payload: { sourceAccountId: string; targetAccountId: string; amount: number; occurredAt: string; description?: string }) =>
    request('/api/v1/transactions/transfers', { method: 'POST', body: JSON.stringify(payload) }, accessToken),
};
