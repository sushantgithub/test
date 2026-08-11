# Aperture

A lightweight mini browser built with React and Vite.

## Features

- Tabbed browsing (new / close / switch; middle-click to close)
- Back, forward, reload, and home
- Omnibox: enter a URL or search query
- Bookmarks bar with star toggle
- New-tab start page with shortcuts
- Keyboard shortcuts: `Ctrl/Cmd+T` new tab, `Ctrl/Cmd+W` close tab, `Ctrl/Cmd+L` focus address bar
- Tabs and bookmarks persist in `localStorage`

## Run

```bash
npm install
npm run dev
```

Build for production:

```bash
npm run build
npm run preview
```

## Note

Many websites block embedding via `X-Frame-Options` / CSP. When a page cannot load in the frame, Aperture shows an open-externally fallback.
