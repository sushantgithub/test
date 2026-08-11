import type { Bookmark, Tab, TabId } from './types';

export const HOME = 'aperture://new';

export const DEFAULT_BOOKMARKS: Bookmark[] = [
  { id: 'b1', title: 'Example', url: 'https://example.com' },
  { id: 'b2', title: 'Wikipedia', url: 'https://en.wikipedia.org/wiki/Main_Page' },
  { id: 'b3', title: 'MDN', url: 'https://developer.mozilla.org/' },
  { id: 'b4', title: 'Archive', url: 'https://web.archive.org/' },
  { id: 'b5', title: 'W3C', url: 'https://www.w3.org/' },
];

export const START_SHORTCUTS = [
  {
    title: 'Example',
    url: 'https://example.com',
    hint: 'Simple demo page',
  },
  {
    title: 'Wikipedia',
    url: 'https://en.wikipedia.org/wiki/Main_Page',
    hint: 'Browse the free encyclopedia',
  },
  {
    title: 'MDN Web Docs',
    url: 'https://developer.mozilla.org/',
    hint: 'Web platform reference',
  },
  {
    title: 'Internet Archive',
    url: 'https://web.archive.org/',
    hint: 'Historical web snapshots',
  },
];

let tabCounter = 0;

export function createTab(url = HOME): Tab {
  tabCounter += 1;
  const id = `tab-${Date.now()}-${tabCounter}`;
  return {
    id,
    title: url === HOME ? 'New Tab' : getHostname(url),
    url,
    history: [url],
    historyIndex: 0,
    loading: url !== HOME,
    blocked: false,
  };
}

export function normalizeInput(input: string): string {
  const trimmed = input.trim();
  if (!trimmed) return HOME;
  if (trimmed === 'aperture://new' || trimmed === 'about:blank') return HOME;

  const looksLikeUrl =
    /^(https?:\/\/)/i.test(trimmed) ||
    (/^[a-z0-9.-]+\.[a-z]{2,}([/:?#].*)?$/i.test(trimmed) && !/\s/.test(trimmed));

  if (looksLikeUrl) {
    return /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
  }

  return `https://duckduckgo.com/?q=${encodeURIComponent(trimmed)}`;
}

export function getHostname(url: string): string {
  if (url === HOME) return 'New Tab';
  try {
    return new URL(url).hostname.replace(/^www\./, '');
  } catch {
    return url;
  }
}

export function displayUrl(url: string): string {
  return url === HOME ? '' : url;
}

export function canGoBack(tab: Tab): boolean {
  return tab.historyIndex > 0;
}

export function canGoForward(tab: Tab): boolean {
  return tab.historyIndex < tab.history.length - 1;
}

export function navigateTab(tab: Tab, rawInput: string): Tab {
  const url = normalizeInput(rawInput);
  if (url === tab.url) {
    return { ...tab, loading: url !== HOME, blocked: false };
  }

  const history = tab.history.slice(0, tab.historyIndex + 1);
  history.push(url);

  return {
    ...tab,
    url,
    title: url === HOME ? 'New Tab' : getHostname(url),
    history,
    historyIndex: history.length - 1,
    loading: url !== HOME,
    blocked: false,
  };
}

export function moveHistory(tab: Tab, direction: -1 | 1): Tab {
  const nextIndex = tab.historyIndex + direction;
  if (nextIndex < 0 || nextIndex >= tab.history.length) return tab;
  const url = tab.history[nextIndex];
  return {
    ...tab,
    url,
    title: url === HOME ? 'New Tab' : getHostname(url),
    historyIndex: nextIndex,
    loading: url !== HOME,
    blocked: false,
  };
}

export function updateTab(tabs: Tab[], id: TabId, patch: Partial<Tab>): Tab[] {
  return tabs.map((tab) => (tab.id === id ? { ...tab, ...patch } : tab));
}
