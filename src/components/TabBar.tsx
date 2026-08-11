import type { Tab } from '../types';
import { HOME } from '../browser';

interface TabBarProps {
  tabs: Tab[];
  activeId: string;
  onSelect: (id: string) => void;
  onClose: (id: string) => void;
  onNew: () => void;
}

export function TabBar({ tabs, activeId, onSelect, onClose, onNew }: TabBarProps) {
  return (
    <div className="tab-bar" role="tablist" aria-label="Open tabs">
      <div className="tab-strip">
        {tabs.map((tab) => {
          const active = tab.id === activeId;
          return (
            <button
              key={tab.id}
              type="button"
              role="tab"
              aria-selected={active}
              className={`tab ${active ? 'tab--active' : ''}`}
              onClick={() => onSelect(tab.id)}
              onAuxClick={(e) => {
                if (e.button === 1) {
                  e.preventDefault();
                  onClose(tab.id);
                }
              }}
            >
              <span className="tab__favicon" aria-hidden>
                {tab.url === HOME ? '◎' : tab.loading ? '◌' : '●'}
              </span>
              <span className="tab__title">{tab.title}</span>
              <span
                className="tab__close"
                role="button"
                tabIndex={0}
                aria-label={`Close ${tab.title}`}
                onClick={(e) => {
                  e.stopPropagation();
                  onClose(tab.id);
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    e.stopPropagation();
                    onClose(tab.id);
                  }
                }}
              >
                ×
              </span>
            </button>
          );
        })}
      </div>
      <button type="button" className="tab-new" onClick={onNew} aria-label="New tab">
        +
      </button>
    </div>
  );
}
