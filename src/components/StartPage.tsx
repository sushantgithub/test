import { START_SHORTCUTS } from '../browser';

interface StartPageProps {
  onNavigate: (url: string) => void;
}

export function StartPage({ onNavigate }: StartPageProps) {
  return (
    <div className="start-page">
      <div className="start-page__glow" aria-hidden />
      <div className="start-page__grain" aria-hidden />

      <header className="start-hero">
        <p className="brand">Aperture</p>
        <h1 className="start-hero__headline">Open the web. Keep the focus.</h1>
        <p className="start-hero__lede">
          Type a URL or search below — tabs, history, and bookmarks stay with you.
        </p>

        <form
          className="start-search"
          onSubmit={(e) => {
            e.preventDefault();
            const data = new FormData(e.currentTarget);
            const q = String(data.get('q') ?? '').trim();
            if (q) onNavigate(q);
          }}
        >
          <input
            name="q"
            className="start-search__input"
            placeholder="Search the web or enter a URL"
            autoFocus
            autoComplete="off"
            spellCheck={false}
          />
          <button type="submit" className="start-search__btn">
            Browse
          </button>
        </form>
      </header>

      <section className="start-shortcuts" aria-label="Suggested destinations">
        {START_SHORTCUTS.map((item, index) => (
          <button
            key={item.url}
            type="button"
            className="shortcut"
            style={{ animationDelay: `${120 + index * 70}ms` }}
            onClick={() => onNavigate(item.url)}
          >
            <span className="shortcut__title">{item.title}</span>
            <span className="shortcut__hint">{item.hint}</span>
          </button>
        ))}
      </section>
    </div>
  );
}
