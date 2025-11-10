import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LoginPage from './LoginPage';

const loginMock = vi.fn();
const setAuthErrorMock = vi.fn();
const apiRequestMock = vi.fn();
const navigateMock = vi.fn();

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock('../context/useAuth', () => ({
  useAuth: () => ({
    login: loginMock,
    error: null,
    setError: setAuthErrorMock,
  }),
}));

vi.mock('../../../api/client', () => ({
  apiRequest: (...args) => apiRequestMock(...args),
}));

describe('LoginPage', () => {
  beforeEach(() => {
    loginMock.mockReset();
    setAuthErrorMock.mockReset();
    apiRequestMock.mockReset();
    navigateMock.mockReset();
  });

  it('submits credentials and roles to the auth endpoint', async () => {
    const user = userEvent.setup();
    apiRequestMock.mockResolvedValue({
      token: 'token',
      userId: 'abc',
      email: 'manager@example.com',
      displayName: 'Manager',
      roles: ['ROLE_USER', 'ROLE_MANAGER'],
    });

    render(<LoginPage />);

    const [emailInput] = screen.getAllByLabelText(/Email/i);
    const [displayNameInput] = screen.getAllByLabelText(/Display Name/i);
    await user.clear(emailInput);
    await user.clear(displayNameInput);

    await user.type(emailInput, 'manager@example.com');
    await user.type(displayNameInput, 'Manager');
    await user.click(screen.getByLabelText('Sign in with manager role'));
    await user.type(
      screen.getByLabelText('Managed Venue IDs (comma separated UUIDs)'),
      '11111111-1111-1111-1111-111111111111'
    );
    const [submitButton] = screen.getAllByRole('button', { name: 'Start Session' });
    await user.click(submitButton);

    await waitFor(() => {
      expect(apiRequestMock).toHaveBeenCalled();
      expect(loginMock).toHaveBeenCalledTimes(1);
      expect(navigateMock).toHaveBeenCalledWith('/', { replace: true });
    });

    expect(apiRequestMock).toHaveBeenCalledWith(
      '/auth/mock',
      {
        method: 'POST',
        data: {
          email: 'manager@example.com',
          displayName: 'Manager',
          roles: ['ROLE_USER', 'ROLE_MANAGER'],
          managedVenueIds: ['11111111-1111-1111-1111-111111111111'],
        },
      },
      null
    );
  });

  it('shows API errors returned from mock login', async () => {
    const user = userEvent.setup();
    apiRequestMock.mockRejectedValue(new Error('Bad credentials'));

    render(<LoginPage />);

    const [emailInput] = screen.getAllByLabelText(/Email/i);
    const [displayNameInput] = screen.getAllByLabelText(/Display Name/i);
    await user.clear(emailInput);
    await user.clear(displayNameInput);

    await user.type(emailInput, 'user@example.com');
    await user.type(displayNameInput, 'User');
    const [submitButton] = screen.getAllByRole('button', { name: 'Start Session' });
    await user.click(submitButton);

    expect(await screen.findByText(/Bad credentials/i)).toBeInTheDocument();
  });
});
