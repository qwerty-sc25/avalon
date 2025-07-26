import { Divider, Skeleton } from "@mui/material";
import {
  Button,
  CardMedia,
  Container,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useSnackbar } from "notistack";
import { useState } from "react";
import API_CLIENT from "../../api/api";
import { BookMetadata } from "../../types/book";
import LinkButton from "../../component/LinkButton";

export const Route = createFileRoute("/books/$bookId")({
  component: RouteComponent,
  params: {
    parse: (params) => {
      const bookId = Number(params.bookId);
      if (isNaN(bookId)) {
        throw new Error("Invalid book ID");
      }
      return { bookId };
    },
  },
});

function RouteComponent() {
  const { bookId } = Route.useParams();
  const { enqueueSnackbar } = useSnackbar();
  const [isOnBookshelf, setIsOnBookshelf] = useState(false);
  const [processing, setProcessing] = useState(false);

  const { data: book, isLoading } = useQuery({
    queryKey: ["books", bookId],
    queryFn: async () => {
      const response = await API_CLIENT.ebookController.getBook(bookId);
      if (!response.isSuccessful) throw new Error(response.errorMessage);
      setIsOnBookshelf(response.data.isOnBookshelf!);
      return response.data as BookMetadata;
    },
  });

  const { data: readProgress } = useQuery({
    queryKey: ["readProgress", bookId],
    queryFn: async () => {
      const response =
        await API_CLIENT.readingProgressController.getMyProgress(bookId);
      if (!response.isSuccessful) throw new Error(response.errorMessage);
      return response.data;
    },
    enabled: book?.isOnBookshelf,
  });

  const handleRegisterBook = async () => {
    setProcessing(true);
    const response =
      await API_CLIENT.ebookShelfController.registerEbook(bookId);
    setProcessing(false);
    if (!response.isSuccessful) {
      switch (response.errorCode) {
        default: {
          enqueueSnackbar(response.errorMessage, { variant: "error" });
          break;
        }
      }
      return;
    }
    enqueueSnackbar("저장이 완료되었습니다!", { variant: "success" });
    setIsOnBookshelf(true);
  };

  if (isLoading) {
    return <PlaceHolder />;
  }

  return (
    <Container sx={{ my: 8 }}>
      <Paper sx={{ p: 4 }} variant="outlined">
        {book && (
          <Stack direction={{ xs: "column", md: "row" }} spacing={4}>
            <CardMedia
              image={book.bookCoverImageURL}
              sx={{
                width: 192,
                height: 256,
                borderRadius: 2,
                mb: 2,
              }}
            />
            <Stack spacing={2} flex={1}>
              <Typography variant="h4">{book.title}</Typography>
              <Typography variant="subtitle1" color="textSecondary">
                {book.author}
              </Typography>
              <Divider />
              <Typography variant="body1" sx={{ whiteSpace: "pre-line" }}>
                {book.description}
              </Typography>
              <Divider />
              <Stack direction="row" spacing={2} alignItems="center">
                <Typography
                  variant="body1"
                  color="textSecondary"
                  sx={{ minWidth: 80 }}
                >
                  출간일
                </Typography>
                <Typography variant="body2">
                  {"publishedAt" in book && book.publishedAt
                    ? new Date(book.publishedAt as string).toLocaleDateString()
                    : "-"}
                </Typography>
              </Stack>

              <Stack spacing={2} direction="row" justifyContent="flex-end">
                {!isOnBookshelf && (
                  <Button
                    variant="contained"
                    onClick={handleRegisterBook}
                    disabled={processing || isOnBookshelf}
                  >
                    {processing ? "담는 중..." : "담기"}
                  </Button>
                )}
                {isOnBookshelf && (
                  <LinkButton
                    to="/reader/$bookId"
                    params={{
                      bookId,
                    }}
                    search={{
                      groupId: undefined,
                      activityId: undefined,
                      temporalProgress: false,
                      location: readProgress?.cfi || null,
                    }}
                    variant="contained"
                  >
                    {`도서 읽기${readProgress?.percentage ? ` (${readProgress.percentage}%)` : ""}`}
                  </LinkButton>
                )}
              </Stack>
            </Stack>
          </Stack>
        )}
      </Paper>
    </Container>
  );
}
function PlaceHolder() {
  return (
    <Container sx={{ my: 8 }}>
      <Paper sx={{ p: 4 }}>
        <Stack direction={{ xs: "column", md: "row" }} spacing={4}>
          <Skeleton
            variant="rectangular"
            width={192}
            height={256}
            sx={{ borderRadius: 2, mb: 2 }}
          />
          <Stack spacing={2} flex={1}>
            <Skeleton variant="text" width="60%" height={48} />
            <Skeleton variant="text" width="40%" height={32} />
            <Divider />
            <Skeleton variant="rectangular" width="100%" height={80} />
            <Divider />
            <Stack direction="row" spacing={2} alignItems="center">
              <Skeleton variant="text" width={80} height={32} />
              <Skeleton variant="text" width={120} height={40} />
            </Stack>
            <Stack direction="row" spacing={2} alignItems="center">
              <Skeleton variant="text" width={80} height={32} />
              <Skeleton variant="text" width={120} height={32} />
            </Stack>
            <Stack spacing={2} direction="row" justifyContent="flex-end">
              <Skeleton variant="rectangular" width={120} height={40} />
              <Skeleton variant="rectangular" width={120} height={40} />
            </Stack>
          </Stack>
        </Stack>
      </Paper>
    </Container>
  );
}
