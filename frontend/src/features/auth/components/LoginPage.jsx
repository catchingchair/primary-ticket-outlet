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
import { useNavigate } from 'react-router-dom';
import { apiRequest } from '../../../api/client';
import { useAuth } from '../context/useAuth';
import { useMockLoginForm } from '../hooks/useMockLoginForm';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, error: authError, setError: setAuthError } = useAuth();
  const {
    formState,
    roles,
    managedVenueIds,
    submitting,
    setSubmitting,
    error,
    setError,
    updateField,
    resetErrors,
  } = useMockLoginForm();

  const handleSubmit = async (event) => {
    event.preventDefault();
    resetErrors();
    setAuthError?.(null);

    if (!formState.email || !formState.displayName) {
      setError('Email and display name are required');
      return;
    }

    setSubmitting(true);
    try {
      const response = await apiRequest(
        '/auth/mock',
        {
          method: 'POST',
          data: {
            email: formState.email,
            displayName: formState.displayName,
            roles,
            managedVenueIds,
          },
        },
        null
      );
      login(response);
      navigate('/', { replace: true });
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
          <Alert severity="error" role="alert">
            {error || authError}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit}>
          <Stack spacing={2}>
            <TextField
              label="Email"
              type="email"
              value={formState.email}
              onChange={(event) => updateField('email', event.target.value)}
              required
            />
            <TextField
              label="Display Name"
              value={formState.displayName}
              onChange={(event) =>
                updateField('displayName', event.target.value)
              }
              required
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formState.asManager}
                  onChange={(event) =>
                    updateField('asManager', event.target.checked)
                  }
                />
              }
              label="Sign in with manager role"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formState.asAdmin}
                  onChange={(event) =>
                    updateField('asAdmin', event.target.checked)
                  }
                />
              }
              label="Sign in with admin role"
            />
            {formState.asManager && (
              <TextField
                label="Managed Venue IDs (comma separated UUIDs)"
                value={formState.managedVenuesRaw}
                onChange={(event) =>
                  updateField('managedVenuesRaw', event.target.value)
                }
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
