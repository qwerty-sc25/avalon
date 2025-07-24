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
  isPurchased: boolean;
  price: number;
};

export type BookRequest = {
  requestId: number;
  title: string;
  author: string;
  description: string;
  size: number;
  price: number;
  coverImageURL: string;
  publisherId: number;
  publisherName: string;
  publisherEmail: string;
  status: string;
  rejectReason: string;
};
