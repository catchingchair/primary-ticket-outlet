import {
  Alert,
  Box,
  Button,
  Checkbox,
  Container,
  FormControlLabel,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useState } from 'react';
import { apiRequest } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { login, error: authError, setError: setAuthError } = useAuth();
  const [email, setEmail] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [asManager, setAsManager] = useState(false);
  const [asAdmin, setAsAdmin] = useState(false);
  const [managedVenuesRaw, setManagedVenuesRaw] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setAuthError?.(null);

    if (!email || !displayName) {
      setError('Email and display name are required');
      return;
    }

    const roles = ['ROLE_USER'];
    if (asManager) roles.push('ROLE_MANAGER');
    if (asAdmin) roles.push('ROLE_ADMIN');

    const managedVenueIds = managedVenuesRaw
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean);

    setSubmitting(true);
    try {
      const response = await apiRequest(
        '/auth/mock',
        {
          method: 'POST',
          data: {
            email,
            displayName,
            roles,
            managedVenueIds,
          },
        },
        null
      );
      login(response);
    } catch (err) {
      console.error(err);
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 12 }}>
      <Stack spacing={3}>
        <Box textAlign="center">
          <Typography variant="h4" gutterBottom>
            Primary Ticket Outlet
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Use the mock SSO form to begin a session.
          </Typography>
        </Box>

        {(error || authError) && (
          <Alert severity="error">{error || authError}</Alert>
        )}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>
            <TextField
              label="Email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
            <TextField
              label="Display Name"
              value={displayName}
              onChange={(event) => setDisplayName(event.target.value)}
              required
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={asManager}
                  onChange={(event) => setAsManager(event.target.checked)}
                />
              }
              label="Sign in with manager role"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={asAdmin}
                  onChange={(event) => setAsAdmin(event.target.checked)}
                />
              }
              label="Sign in with admin role"
            />
            {asManager && (
              <TextField
                label="Managed Venue IDs (comma separated UUIDs)"
                value={managedVenuesRaw}
                onChange={(event) => setManagedVenuesRaw(event.target.value)}
                helperText="Optional. Use sample IDs from docs to gain manager access."
              />
            )}
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={submitting}
            >
              {submitting ? 'Signing in…' : 'Start Session'}
            </Button>
          </Stack>
        </Box>
      </Stack>
    </Container>
  );
}

