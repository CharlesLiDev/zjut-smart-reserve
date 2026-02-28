import { clearAuthSession, getAuthToken } from '@/utils/auth';

export type ApiResult<T> = {
  code: number;
  msg: string;
  data: T;
};

type RequestOptions = {
  method?: string;
  query?: Record<string, string | number | boolean | null | undefined>;
  body?: unknown;
  timeoutMs?: number;
};

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim() || 'http://localhost:8080';

function buildUrl(path: string, query?: RequestOptions['query']) {
  const base = API_BASE_URL.endsWith('/') ? API_BASE_URL.slice(0, -1) : API_BASE_URL;
  const p = path.startsWith('/') ? path : `/${path}`;
  const url = new URL(`${base}${p}`);
  if (query) {
    Object.entries(query).forEach(([k, v]) => {
      if (v === null || v === undefined || v === '') return;
      url.searchParams.set(k, String(v));
    });
  }
  return url.toString();
}

export async function apiRequest<T = unknown>(path: string, options: RequestOptions = {}) {
  const controller = new AbortController();
  const timer = window.setTimeout(() => controller.abort(), options.timeoutMs ?? 10_000);
  try {
    const token = getAuthToken();
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    };
    if (token) headers.Authorization = `Bearer ${token}`;

    const res = await fetch(buildUrl(path, options.query), {
      method: options.method ?? (options.body ? 'POST' : 'GET'),
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      signal: controller.signal
    });

    let payload: ApiResult<T> | null = null;
    try {
      payload = (await res.json()) as ApiResult<T>;
    } catch {
      throw new Error('服务返回数据格式异常');
    }

    if (res.status === 401) {
      clearAuthSession();
      window.alert('登录已过期，请重新登录');
      window.location.href = '/login';
      throw new Error('登录已过期');
    }

    if (res.status === 403) {
      window.location.href = '/403';
      throw new Error('没有权限访问该功能');
    }

    if (!res.ok || !payload || payload.code !== 200) {
      throw new Error(payload?.msg || `请求失败(${res.status})`);
    }

    return payload.data;
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error('请求超时，请检查网络');
    }
    throw error;
  } finally {
    window.clearTimeout(timer);
  }
}

