export type Book = {
  // 공통 필드
  id: number;
  title: string;
  bookCoverImageURL: string;
  author: string;
  description: string;
  size: number;
  isOnBookshelf: boolean;
};

export type BookMetadata = {
  id: number;
  title: string;
  bookCoverImageURL: string;
  author: string;
  description: string;
  size: number;
  isOnBookshelf: boolean;
};
