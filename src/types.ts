export type TabId = string;

export interface Tab {
  id: TabId;
  title: string;
  url: string;
  history: string[];
  historyIndex: number;
  loading: boolean;
  blocked: boolean;
}

export interface Bookmark {
  id: string;
  title: string;
  url: string;
}
