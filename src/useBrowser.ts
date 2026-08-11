import { useCallback, useEffect, useState } from 'react';
import {
  canGoBack,
  canGoForward,
  createTab,
  DEFAULT_BOOKMARKS,
  HOME,
  moveHistory,
  navigateTab,
  updateTab,
} from './browser';
import type { Bookmark, Tab, TabId } from './types';

const TABS_KEY = 'aperture.tabs';
const ACTIVE_KEY = 'aperture.active';
const BOOKMARKS_KEY = 'aperture.bookmarks';

function loadTabs(): { tabs: Tab[]; activeId: TabId } {
  try {
    const raw = localStorage.getItem(TABS_KEY);
    const active = localStorage.getItem(ACTIVE_KEY);
    if (raw) {
      const tabs = JSON.parse(raw) as Tab[];
      if (Array.isArray(tabs) && tabs.length > 0) {
        const activeId =
          active && tabs.some((t) => t.id === active) ? active : tabs[0].id;
        return {
          tabs: tabs.map((t) => ({ ...t, loading: false, blocked: false })),
          activeId,
        };
      }
    }
  } catch {
    /* ignore corrupt storage */
  }
  const tab = createTab();
  return { tabs: [tab], activeId: tab.id };
}

function loadBookmarks(): Bookmark[] {
  try {
    const raw = localStorage.getItem(BOOKMARKS_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as Bookmark[];
      if (Array.isArray(parsed) && parsed.length > 0) return parsed;
    }
  } catch {
    /* ignore */
  }
  return DEFAULT_BOOKMARKS;
}

export function useBrowser() {
  const initial = loadTabs();
  const [tabs, setTabs] = useState<Tab[]>(initial.tabs);
  const [activeId, setActiveId] = useState<TabId>(initial.activeId);
  const [bookmarks, setBookmarks] = useState<Bookmark[]>(loadBookmarks);
  const [addressDraft, setAddressDraft] = useState('');

  const activeTab = tabs.find((t) => t.id === activeId) ?? tabs[0];

  useEffect(() => {
    localStorage.setItem(TABS_KEY, JSON.stringify(tabs));
    localStorage.setItem(ACTIVE_KEY, activeId);
  }, [tabs, activeId]);

  useEffect(() => {
    localStorage.setItem(BOOKMARKS_KEY, JSON.stringify(bookmarks));
  }, [bookmarks]);

  useEffect(() => {
    if (!activeTab) return;
    setAddressDraft(activeTab.url === HOME ? '' : activeTab.url);
  }, [activeTab]);

  const selectTab = useCallback((id: TabId) => {
    setActiveId(id);
  }, []);

  const openTab = useCallback((url = HOME) => {
    const tab = createTab(url);
    setTabs((prev) => [...prev, tab]);
    setActiveId(tab.id);
  }, []);

  const closeTab = useCallback(
    (id: TabId) => {
      setTabs((prev) => {
        if (prev.length === 1) {
          const fresh = createTab();
          setActiveId(fresh.id);
          return [fresh];
        }
        const index = prev.findIndex((t) => t.id === id);
        const next = prev.filter((t) => t.id !== id);
        if (id === activeId) {
          const fallback = next[Math.max(0, index - 1)] ?? next[0];
          setActiveId(fallback.id);
        }
        return next;
      });
    },
    [activeId],
  );

  const navigate = useCallback(
    (input: string) => {
      if (!activeTab) return;
      setTabs((prev) =>
        updateTab(prev, activeTab.id, navigateTab(activeTab, input)),
      );
    },
    [activeTab],
  );

  const goBack = useCallback(() => {
    if (!activeTab || !canGoBack(activeTab)) return;
    setTabs((prev) => updateTab(prev, activeTab.id, moveHistory(activeTab, -1)));
  }, [activeTab]);

  const goForward = useCallback(() => {
    if (!activeTab || !canGoForward(activeTab)) return;
    setTabs((prev) => updateTab(prev, activeTab.id, moveHistory(activeTab, 1)));
  }, [activeTab]);

  const reload = useCallback(() => {
    if (!activeTab || activeTab.url === HOME) return;
    setTabs((prev) =>
      updateTab(prev, activeTab.id, {
        loading: true,
        blocked: false,
        url: activeTab.url,
      }),
    );
    // Force iframe remount via loading flag + key elsewhere
    window.setTimeout(() => {
      setTabs((prev) =>
        updateTab(prev, activeTab.id, {
          loading: true,
          blocked: false,
        }),
      );
    }, 0);
  }, [activeTab]);

  const goHome = useCallback(() => {
    navigate(HOME);
  }, [navigate]);

  const markLoaded = useCallback((id: TabId, title?: string) => {
    setTabs((prev) =>
      updateTab(prev, id, {
        loading: false,
        ...(title ? { title } : {}),
      }),
    );
  }, []);

  const markBlocked = useCallback((id: TabId) => {
    setTabs((prev) =>
      updateTab(prev, id, {
        loading: false,
        blocked: true,
      }),
    );
  }, []);

  const toggleBookmark = useCallback(() => {
    if (!activeTab || activeTab.url === HOME) return;
    setBookmarks((prev) => {
      const existing = prev.find((b) => b.url === activeTab.url);
      if (existing) return prev.filter((b) => b.id !== existing.id);
      return [
        ...prev,
        {
          id: `bm-${Date.now()}`,
          title: activeTab.title,
          url: activeTab.url,
        },
      ];
    });
  }, [activeTab]);

  const isBookmarked =
    !!activeTab &&
    activeTab.url !== HOME &&
    bookmarks.some((b) => b.url === activeTab.url);

  return {
    tabs,
    activeTab,
    activeId,
    bookmarks,
    addressDraft,
    setAddressDraft,
    selectTab,
    openTab,
    closeTab,
    navigate,
    goBack,
    goForward,
    reload,
    goHome,
    markLoaded,
    markBlocked,
    toggleBookmark,
    isBookmarked,
    canBack: activeTab ? canGoBack(activeTab) : false,
    canForward: activeTab ? canGoForward(activeTab) : false,
  };
}
