import {
  AppBar,
  Box,
  Button,
  Container,
  CssBaseline,
  Stack,
  Toolbar,
  Typography,
} from '@mui/material';
import { useEffect, useMemo, useState } from 'react';
import LoginPage from './components/LoginPage';
import RoleSwitcher from './components/RoleSwitcher';
import { AuthProvider, useAuth } from './context/AuthContext';
import AdminView from './views/AdminView';
import ManagerView from './views/ManagerView';
import UserView from './views/UserView';
import './App.css';

function AppShell() {
  const { token, user, roles, logout } = useAuth();
  const [activeRole, setActiveRole] = useState(roles[0] ?? 'ROLE_USER');

  const availableRoles = useMemo(
    () => (roles?.length ? roles : ['ROLE_USER']),
    [roles]
  );

  useEffect(() => {
    setActiveRole(availableRoles[0]);
  }, [availableRoles]);

  if (!token) {
    return <LoginPage />;
  }

  const renderView = () => {
    switch (activeRole) {
      case 'ROLE_MANAGER':
        return <ManagerView />;
      case 'ROLE_ADMIN':
        return <AdminView />;
      case 'ROLE_USER':
      default:
        return <UserView />;
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static" color="primary" enableColorOnDark>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Primary Ticket Outlet
          </Typography>
          <Stack direction="row" spacing={2} alignItems="center">
            <Typography variant="body2">
              {user?.displayName || user?.email}
            </Typography>
            <Button color="inherit" onClick={logout}>
              Sign out
            </Button>
          </Stack>
        </Toolbar>
      </AppBar>
      <Container sx={{ py: 4 }}>
        <Stack spacing={4}>
          <RoleSwitcher
            roles={availableRoles}
            activeRole={activeRole}
            onRoleChange={setActiveRole}
          />
          {renderView()}
        </Stack>
      </Container>
    </Box>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <CssBaseline />
      <AppShell />
    </AuthProvider>
  );
}
