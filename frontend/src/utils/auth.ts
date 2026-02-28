export type AppRole = 'user' | 'admin' | 'super_admin';

export type AuthSession = {
  token: string;
  role: AppRole;
  userId?: number | string | null;
  username?: string;
  realName?: string;
  isFirstLogin?: boolean;
};

export function normalizeRole(rawRole?: string): AppRole {
  const role = (rawRole ?? '').toUpperCase();
  if (role === 'SUPER_ADMIN' || role === 'SYS_ADMIN' || role === 'SUPER') return 'super_admin';
  if (role === 'ADMIN' || role === 'VENUE_ADMIN' || role === 'MANAGER') return 'admin';
  return 'user';
}

export function getAuthSession(): AuthSession | null {
  const raw = localStorage.getItem('auth') ?? sessionStorage.getItem('auth');
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthSession;
  } catch {
    return null;
  }
}

export function setAuthSession(session: AuthSession, remember = false) {
  const storage = remember ? localStorage : sessionStorage;
  const anotherStorage = remember ? sessionStorage : localStorage;
  anotherStorage.removeItem('auth');
  storage.setItem('auth', JSON.stringify(session));
}

export function clearAuthSession() {
  localStorage.removeItem('auth');
  sessionStorage.removeItem('auth');
}

export function getAuthToken() {
  return getAuthSession()?.token ?? '';
}

