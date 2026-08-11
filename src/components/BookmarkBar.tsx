import type { Bookmark } from '../types';

interface BookmarkBarProps {
  bookmarks: Bookmark[];
  onOpen: (url: string) => void;
}

export function BookmarkBar({ bookmarks, onOpen }: BookmarkBarProps) {
  return (
    <div className="bookmark-bar" aria-label="Bookmarks">
      {bookmarks.map((bookmark) => (
        <button
          key={bookmark.id}
          type="button"
          className="bookmark-chip"
          onClick={() => onOpen(bookmark.url)}
          title={bookmark.url}
        >
          {bookmark.title}
        </button>
      ))}
    </div>
  );
}
