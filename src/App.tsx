import { useEffect, useState } from 'react';
import { BookmarkBar } from './components/BookmarkBar';
import { NavBar } from './components/NavBar';
import { TabBar } from './components/TabBar';
import { Viewport } from './components/Viewport';
import { useBrowser } from './useBrowser';
import './App.css';

export default function App() {
  const browser = useBrowser();
  const [reloadToken, setReloadToken] = useState(0);
  const { openTab, closeTab, activeTab } = browser;

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const meta = e.metaKey || e.ctrlKey;
      if (meta && e.key.toLowerCase() === 't') {
        e.preventDefault();
        openTab();
      }
      if (meta && e.key.toLowerCase() === 'w') {
        e.preventDefault();
        if (activeTab) closeTab(activeTab.id);
      }
      if (meta && e.key.toLowerCase() === 'l') {
        e.preventDefault();
        const input = document.querySelector<HTMLInputElement>('.omnibox__input');
        input?.focus();
        input?.select();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [openTab, closeTab, activeTab]);

  if (!activeTab) return null;

  return (
    <div className="browser-shell">
      <header className="chrome">
        <div className="chrome__brand" aria-hidden>
          <span className="chrome__mark" />
          <span className="chrome__name">Aperture</span>
        </div>
        <TabBar
          tabs={browser.tabs}
          activeId={browser.activeId}
          onSelect={browser.selectTab}
          onClose={browser.closeTab}
          onNew={() => browser.openTab()}
        />
        <NavBar
          canBack={browser.canBack}
          canForward={browser.canForward}
          isBookmarked={browser.isBookmarked}
          addressDraft={browser.addressDraft}
          loading={activeTab.loading}
          onBack={browser.goBack}
          onForward={browser.goForward}
          onReload={() => {
            browser.reload();
            setReloadToken((t) => t + 1);
          }}
          onHome={browser.goHome}
          onToggleBookmark={browser.toggleBookmark}
          onAddressChange={browser.setAddressDraft}
          onNavigate={browser.navigate}
        />
        <BookmarkBar bookmarks={browser.bookmarks} onOpen={browser.navigate} />
      </header>

      <main className="browser-main">
        <Viewport
          tab={activeTab}
          reloadToken={reloadToken}
          onNavigate={browser.navigate}
          onLoaded={browser.markLoaded}
          onBlocked={browser.markBlocked}
        />
      </main>
    </div>
  );
}
