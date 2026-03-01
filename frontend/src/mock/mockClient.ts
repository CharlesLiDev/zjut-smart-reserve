type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue };

const promiseCache = new Map<string, Promise<unknown>>();

function joinBaseUrl(base: string, relative: string) {
  const baseNorm = base.endsWith('/') ? base : `${base}/`;
  const relNorm = relative.startsWith('/') ? relative.slice(1) : relative;
  return `${baseNorm}${relNorm}`;
}

export async function fetchMockJson<T = JsonValue>(relativePath: string): Promise<T> {
  const baseUrl = import.meta.env.BASE_URL ?? '/';
  const url = joinBaseUrl(baseUrl, relativePath);

  const cached = promiseCache.get(url) as Promise<T> | undefined;
  if (cached) return cached;

  const p = (async () => {
    const res = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!res.ok) {
      throw new Error(`Mock JSON 请求失败：${res.status} ${res.statusText} (${url})`);
    }
    return (await res.json()) as T;
  })();

  promiseCache.set(url, p);
  return p;
}

