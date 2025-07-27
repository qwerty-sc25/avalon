import {
  Toolbar,
  AppBar as MuiAppBar,
  Button,
  Divider,
  Avatar,
  Stack,
  IconButton,
  BottomNavigation,
  BottomNavigationAction,
} from "@mui/material";
import { createLink } from "@tanstack/react-router";
import { useAtom, useAtomValue } from "jotai";
import { useLocation } from "@tanstack/react-router";
import { AuthState } from "../states/auth";
import { Role } from "../types/role";
import useLogout from "../api/login/useLogout";
import LinkButton from "./LinkButton";
import NotificationButton from "./NotificationButton";
import State from "../states";
import {
  Menu,
  Nightlight,
  Sunny,
  Home,
  Person,
  Book,
  Group,
} from "@mui/icons-material";
import { useState } from "react";
import SideNavigationBar, { NavigationItem } from "./SideNavigatorBar";

export default function AppBar(props: {
  sideNavigationBarItemsWithGroups: NavigationItem[][];
}) {
  const { sideNavigationBarItemsWithGroups } = props;
  const user = useAtomValue(AuthState.user);
  const [colorScheme, setColorScheme] = useAtom(State.UI.userColorScheme);
  const { logout } = useLogout();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  const isAdmin = user && user.role === Role.ROLE_ADMIN;

  const onColorSchemeChangeButtonClicked = () => {
    setColorScheme((prev) => {
      const next = prev === "light" ? "dark" : "light";
      localStorage.setItem("colorScheme", next);
      return next;
    });
  };

  // Get current bottom navigation value based on pathname
  const getBottomNavValue = () => {
    const pathname = location.pathname;
    if (pathname === "/") return 0;
    if (pathname.startsWith("/books")) return 1;
    if (pathname.startsWith("/groups")) return 2;
    if (pathname.startsWith("/mypage")) return 3;
    return 0;
  };

  return (
    <>
      <SideNavigationBar
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        itemsWithGroups={sideNavigationBarItemsWithGroups}
      />
      <MuiAppBar
        position="fixed"
        color="transparent"
        sx={{
          backdropFilter: "blur(10px)",
        }}
        elevation={0}
      >
        <Toolbar sx={{ display: "flex", justifyContent: "space-between" }}>
          <Stack direction="row" spacing={1} alignItems="center">
            <IconButton
              onClick={() => {
                setSidebarOpen((prev) => !prev);
              }}
            >
              <Menu />
            </IconButton>
            <LogoButton to="/" sx={{ display: { xs: "none", sm: "flex" } }}>
              <img src="/logoTitle.png" alt="Logo" height={40} />
            </LogoButton>
          </Stack>
          <Stack direction="row" spacing={1}>
            <IconButton onClick={onColorSchemeChangeButtonClicked}>
              {colorScheme === "light" ? <Sunny /> : <Nightlight />}
            </IconButton>
            <Divider orientation="vertical" flexItem variant="middle" />
            {user ? (
              <>
                <LinkButton
                  color="inherit"
                  to={isAdmin ? "/mypage/admin" : "/mypage"}
                >
                  <Stack
                    direction="row"
                    alignItems="center"
                    sx={{ textWrap: "nowrap" }}
                  >
                    <Avatar
                      src={user.profileImageURL}
                      sx={{ width: 24, height: 24, mr: 1 }}
                    />
                    {user.role === Role.ROLE_USER ? user.nickname : "ADMIN"}
                  </Stack>
                </LinkButton>
                <NotificationButton />
                <Button
                  color="inherit"
                  onClick={logout}
                  sx={{ textWrap: "nowrap" }}
                >
                  로그아웃
                </Button>
              </>
            ) : (
              <LinkButton color="inherit" to={"/login"}>
                로그인
              </LinkButton>
            )}
          </Stack>
        </Toolbar>
      </MuiAppBar>

      <BottomNavigation
        value={getBottomNavValue()}
        showLabels
        sx={{
          display: { xs: "flex", sm: "none" },
          height: 64,
          position: "fixed",
          bottom: 0,
          left: 0,
          right: 0,
          zIndex: 1000,
        }}
      >
        <LinkBottomNavigationAction label="홈" icon={<Home />} to="/" />
        <LinkBottomNavigationAction
          label="도서"
          icon={<Book />}
          to="/books"
          search={{ title: "" }}
        />
        <LinkBottomNavigationAction
          label="모임"
          icon={<Group />}
          to="/groups"
          search={{ searchTerms: [] }}
        />
        {user && (
          <LinkBottomNavigationAction
            label="마이페이지"
            icon={<Person />}
            to={isAdmin ? "/mypage/admin" : "/mypage"}
          />
        )}
      </BottomNavigation>
    </>
  );
}

const LogoButton = createLink(Button);
const LinkBottomNavigationAction = createLink(BottomNavigationAction);
