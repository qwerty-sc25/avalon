import { useState, useCallback } from "react";
import {
  Container,
  Paper,
  Typography,
  Box,
  Card,
  CardContent,
  Grid,
  Button,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Avatar,
  Stack,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Divider,
  TablePagination,
} from "@mui/material";
import {
  AdminPanelSettings,
  Person,
  MoreVert,
  Email,
  Search,
  Refresh,
  Workspaces,
} from "@mui/icons-material";
import { createFileRoute } from "@tanstack/react-router";
import { useAtomValue } from "jotai";
import { AuthState } from "../../../states/auth";
import { Role } from "../../../types/role";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import API_CLIENT from "../../../api/api";

export const Route = createFileRoute("/mypage/admin/")({
  component: RouteComponent,
});

// 타입 정의
interface User {
  userId: number;
  nickname: string;
  profileImageURL: string;
}

interface PaginatedResponse<T> {
  content: T[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel({ children, value, index }: TabPanelProps) {
  return (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
    </div>
  );
}

// 상태별 색상 및 텍스트

function RouteComponent() {
  const user = useAtomValue(AuthState.user);
  const isAdmin = user && user?.role === Role.ROLE_ADMIN;
  const queryClient = useQueryClient();

  const [currentTab, setCurrentTab] = useState(0);

  // 각 탭별 독립적인 검색 및 필터 상태
  const [userSearchTerm, setUserSearchTerm] = useState("");

  // 페이지네이션 상태
  const [userPage, setUserPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  // 다이얼로그 상태
  const [openUserDialog, setOpenUserDialog] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  // 유저 목록 조회 (페이지네이션 적용)
  const { data: usersResponse } = useQuery({
    queryKey: ["adminUsers", userPage, rowsPerPage],
    queryFn: async () => {
      const response = await API_CLIENT.adminController.fetchUsers({
        page: userPage,
        size: rowsPerPage,
      });
      if (!response.isSuccessful) {
        throw new Error(response.errorMessage);
      }
      return response.data as PaginatedResponse<User>;
    },
    enabled: isAdmin,
    initialData: {
      content: [] as User[],
      currentPage: 0,
      totalItems: 0,
      totalPages: 0,
    },
  });

  // 현재 탭의 데이터
  const users = usersResponse?.content || [];

  const { data: groupsResponse } = useQuery({
    queryKey: ["groupsCount"],
    queryFn: async () => {
      const response = await API_CLIENT.groupController.getAllGroups();
      if (!response.isSuccessful) {
        throw new Error(response.errorCode);
      }
      return response.data;
    },
    enabled: isAdmin,
  });

  const groupsCount = groupsResponse?.totalItems;

  // 페이지 변경 핸들러들

  const handleUserPageChange = useCallback((_: any, newPage: number) => {
    setUserPage(newPage);
  }, []);

  const handleRowsPerPageChange = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      const newRowsPerPage = parseInt(event.target.value, 10);
      setRowsPerPage(newRowsPerPage);
      // 모든 탭의 페이지를 0으로 리셋
      setUserPage(0);
    },
    [],
  );

  const filteredUsers = users.filter((user) =>
    user.nickname.toLowerCase().includes(userSearchTerm.toLowerCase()),
  );

  // 새로고침 핸들러
  const handleRefresh = useCallback(() => {
    queryClient.invalidateQueries({ queryKey: ["adminPublishers"] });
    queryClient.invalidateQueries({ queryKey: ["adminUsers"] });
    queryClient.invalidateQueries({ queryKey: ["adminBookRequests"] });
    queryClient.invalidateQueries({ queryKey: ["adminPublisherStats"] });
  }, [queryClient]);

  // 요약 통계 카드
  const SummaryCards = () => (
    <Grid container spacing={3} sx={{ mb: 4 }}>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <Card variant="outlined" sx={{ height: "100%" }}>
          <CardContent
            sx={{
              height: "100%",
              display: "flex",
              flexDirection: "column",
              justifyContent: "center",
            }}
          >
            <Box
              display="flex"
              alignItems="center"
              justifyContent="space-between"
            >
              <Box>
                <Typography
                  color="textSecondary"
                  gutterBottom
                  variant="overline"
                >
                  총 사용자
                </Typography>
                <Typography variant="h4">
                  {usersResponse?.totalItems || 0}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  활성 사용자
                </Typography>
              </Box>
              <Avatar sx={{ bgcolor: "success.main" }}>
                <Person />
              </Avatar>
            </Box>
          </CardContent>
        </Card>
      </Grid>

      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <Card variant="outlined" sx={{ height: "100%" }}>
          <CardContent
            sx={{
              height: "100%",
              display: "flex",
              flexDirection: "column",
              justifyContent: "center",
            }}
          >
            <Box
              display="flex"
              alignItems="center"
              justifyContent="space-between"
            >
              <Box>
                <Typography
                  color="textSecondary"
                  gutterBottom
                  variant="overline"
                >
                  총 그룹 수
                </Typography>
                <Typography variant="h4" color="success.main">
                  {groupsCount || 0}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  개설된 그룹
                </Typography>
              </Box>
              <Avatar sx={{ bgcolor: "warning.main" }}>
                <Workspaces />
              </Avatar>
            </Box>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );

  if (!isAdmin) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">관리자 권한이 필요합니다.</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      {/* 헤더 */}
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={4}
      >
        <Box>
          <Stack direction="row" spacing={2} alignItems="center">
            <AdminPanelSettings fontSize="large" color="primary" />
            <Typography variant="h4" gutterBottom>
              관리자 대시보드
            </Typography>
          </Stack>
          <Typography variant="body1" color="textSecondary">
            일반 사용자, 출판사 및 출판물 관리
          </Typography>
        </Box>
      </Box>

      {/* 요약 통계 */}
      <SummaryCards />

      {/* 탭 네비게이션 */}
      <Paper sx={{ mb: 3 }} variant="outlined">
        <Tabs
          value={currentTab}
          onChange={handleTabChange}
          sx={{ borderBottom: 1, borderColor: "divider" }}
        >
          <Tab
            icon={<Person />}
            label={`사용자 관리 (${usersResponse?.totalItems || 0})`}
          />
        </Tabs>

        {/* 사용자 관리 탭 */}
        <TabPanel value={currentTab} index={1}>
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={2}
            alignItems="center"
            marginLeft={2}
            marginRight={2}
          >
            <TextField
              placeholder="검색..."
              value={userSearchTerm}
              onChange={(e) => setUserSearchTerm(e.target.value)}
              size="small"
              sx={{ flexGrow: 1 }}
              InputProps={{
                startAdornment: (
                  <Search sx={{ color: "action.active", mr: 1 }} />
                ),
              }}
            />

            <Button
              variant="outlined"
              startIcon={<Refresh />}
              onClick={handleRefresh}
            >
              새로고침
            </Button>
          </Stack>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>사용자명</TableCell>
                  <TableCell>이메일</TableCell>
                  <TableCell>등록일</TableCell>
                  <TableCell>상태</TableCell>
                  <TableCell align="center">관리</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredUsers.map((user) => (
                  <TableRow
                    key={user.userId}
                    hover
                    sx={{ cursor: "pointer" }}
                    onClick={() => {
                      setSelectedUser(user);
                      setOpenUserDialog(true);
                    }}
                  >
                    <TableCell>
                      <Typography variant="subtitle2" fontWeight={600}>
                        {user.nickname}
                      </Typography>
                    </TableCell>
                    <TableCell>이메일</TableCell>
                    <TableCell>{new Date().toLocaleDateString()}</TableCell>
                    <TableCell>
                      <Chip label="활성" color="success" size="small" />
                    </TableCell>
                    <TableCell align="center">
                      <IconButton size="small" color="primary">
                        <MoreVert />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={usersResponse?.totalItems || 0}
            page={userPage}
            onPageChange={handleUserPageChange}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleRowsPerPageChange}
            labelRowsPerPage="페이지당 행 수:"
            labelDisplayedRows={({ from, to, count }) =>
              `${from}-${to} / ${count !== -1 ? count : `${to}개 이상`}`
            }
          />
        </TabPanel>
      </Paper>

      {/* 사용자 상세 다이얼로그 */}
      {selectedUser && (
        <UserDetailDialog
          user={selectedUser}
          open={openUserDialog}
          onClose={() => setOpenUserDialog(false)}
        />
      )}
    </Container>
  );
}

// 사용자 상세 다이얼로그
function UserDetailDialog({
  user,
  open,
  onClose,
}: {
  user: User;
  open: boolean;
  onClose(): void;
}) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>사용자 상세 정보</DialogTitle>
      <DialogContent>
        <Stack spacing={3}>
          <Box>
            <Typography variant="h5" gutterBottom>
              {user.nickname}
            </Typography>
          </Box>

          <Divider />

          <Stack spacing={2}>
            <Box display="flex" alignItems="center" gap={1}>
              <Email fontSize="small" color="action" />
              <Typography variant="body1">이메일</Typography>
            </Box>
            <Typography variant="body2" color="textSecondary">
              등록일: {new Date().toLocaleDateString()}
            </Typography>
          </Stack>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>닫기</Button>
      </DialogActions>
    </Dialog>
  );
}
