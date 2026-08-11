interface NavBarProps {
  canBack: boolean;
  canForward: boolean;
  isBookmarked: boolean;
  addressDraft: string;
  loading: boolean;
  onBack: () => void;
  onForward: () => void;
  onReload: () => void;
  onHome: () => void;
  onToggleBookmark: () => void;
  onAddressChange: (value: string) => void;
  onNavigate: (value: string) => void;
}

export function NavBar({
  canBack,
  canForward,
  isBookmarked,
  addressDraft,
  loading,
  onBack,
  onForward,
  onReload,
  onHome,
  onToggleBookmark,
  onAddressChange,
  onNavigate,
}: NavBarProps) {
  return (
    <div className="nav-bar">
      <div className="nav-controls">
        <button
          type="button"
          className="icon-btn"
          disabled={!canBack}
          onClick={onBack}
          aria-label="Back"
          title="Back"
        >
          ←
        </button>
        <button
          type="button"
          className="icon-btn"
          disabled={!canForward}
          onClick={onForward}
          aria-label="Forward"
          title="Forward"
        >
          →
        </button>
        <button
          type="button"
          className="icon-btn"
          onClick={onReload}
          aria-label="Reload"
          title="Reload"
        >
          {loading ? '…' : '↻'}
        </button>
        <button
          type="button"
          className="icon-btn"
          onClick={onHome}
          aria-label="Home"
          title="Home"
        >
          ⌂
        </button>
      </div>

      <form
        className="omnibox"
        onSubmit={(e) => {
          e.preventDefault();
          onNavigate(addressDraft);
        }}
      >
        <span
          className={`omnibox__lock ${addressDraft.startsWith('https://') ? 'is-secure' : ''}`}
          aria-hidden
        />
        <input
          className="omnibox__input"
          type="text"
          value={addressDraft}
          onChange={(e) => onAddressChange(e.target.value)}
          placeholder="Search or enter address"
          spellCheck={false}
          autoComplete="off"
          aria-label="Address bar"
        />
        <button type="submit" className="omnibox__go" aria-label="Go">
          Go
        </button>
      </form>

      <button
        type="button"
        className={`icon-btn icon-btn--bookmark ${isBookmarked ? 'is-active' : ''}`}
        onClick={onToggleBookmark}
        aria-label={isBookmarked ? 'Remove bookmark' : 'Add bookmark'}
        title={isBookmarked ? 'Remove bookmark' : 'Bookmark this page'}
      >
        {isBookmarked ? '★' : '☆'}
      </button>
    </div>
  );
}
