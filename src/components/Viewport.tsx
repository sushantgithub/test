import { useEffect, useRef, useState } from 'react';
import { getHostname, HOME } from '../browser';
import type { Tab } from '../types';
import { StartPage } from './StartPage';

interface ViewportProps {
  tab: Tab;
  reloadToken: number;
  onNavigate: (url: string) => void;
  onLoaded: (id: string, title?: string) => void;
  onBlocked: (id: string) => void;
}

export function Viewport({
  tab,
  reloadToken,
  onNavigate,
  onLoaded,
  onBlocked,
}: ViewportProps) {
  const frameRef = useRef<HTMLIFrameElement>(null);
  const [frameKey, setFrameKey] = useState(0);

  useEffect(() => {
    setFrameKey((k) => k + 1);
  }, [tab.url, reloadToken]);

  useEffect(() => {
    if (tab.url === HOME || tab.blocked) return;

    const timer = window.setTimeout(() => {
      try {
        const doc = frameRef.current?.contentDocument;
        // Cross-origin frames throw; if we can read and it's empty-ish, treat as blocked
        if (doc && doc.location.href === 'about:blank') {
          onBlocked(tab.id);
        }
      } catch {
        // Cross-origin usually means the page loaded; leave as-is
      }
    }, 4500);

    return () => window.clearTimeout(timer);
  }, [tab.id, tab.url, tab.blocked, frameKey, onBlocked]);

  if (tab.url === HOME) {
    return (
      <div className="viewport">
        <StartPage onNavigate={onNavigate} />
      </div>
    );
  }

  if (tab.blocked) {
    return (
      <div className="viewport">
        <div className="blocked">
          <p className="blocked__brand">Aperture</p>
          <h2 className="blocked__title">This site won’t load in the frame</h2>
          <p className="blocked__copy">
            Many sites block embedding for security. You can open{' '}
            <strong>{getHostname(tab.url)}</strong> in a new window instead.
          </p>
          <div className="blocked__actions">
            <a
              className="btn btn--primary"
              href={tab.url}
              target="_blank"
              rel="noopener noreferrer"
            >
              Open externally
            </a>
            <button type="button" className="btn btn--ghost" onClick={() => onNavigate(HOME)}>
              Back to start
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="viewport viewport--frame">
      {tab.loading && <div className="viewport__loading" aria-hidden />}
      <iframe
        key={`${tab.id}-${frameKey}`}
        ref={frameRef}
        className="viewport__iframe"
        src={tab.url}
        title={tab.title}
        sandbox="allow-scripts allow-same-origin allow-forms allow-popups allow-popups-to-escape-sandbox"
        referrerPolicy="no-referrer-when-downgrade"
        onLoad={() => {
          let title = getHostname(tab.url);
          try {
            const docTitle = frameRef.current?.contentDocument?.title;
            if (docTitle) title = docTitle;
          } catch {
            /* cross-origin */
          }
          onLoaded(tab.id, title);
        }}
        onError={() => onBlocked(tab.id)}
      />
    </div>
  );
}
