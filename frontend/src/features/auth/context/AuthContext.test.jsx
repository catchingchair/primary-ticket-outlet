import { renderHook, act, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from './AuthContext';
import { useAuth } from './useAuth';

const apiRequest = vi.fn();
let consoleErrorSpy;

vi.mock('../../../api/client', () => ({
  apiRequest: (...args) => apiRequest(...args),
}));

const wrapper = ({ children }) => <AuthProvider>{children}</AuthProvider>;

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    apiRequest.mockReset();
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    consoleErrorSpy?.mockRestore();
  });

  it('stores token, user, and roles on login', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    const response = {
      token: 'test-token',
      userId: '11111111-1111-1111-1111-111111111111',
      email: 'user@example.com',
      displayName: 'Test User',
      roles: ['ROLE_USER', 'ROLE_MANAGER'],
    };

    act(() => {
      result.current.login(response);
    });

    await waitFor(() => {
      expect(result.current.token).toBe('test-token');
      expect(result.current.roles).toEqual(['ROLE_USER', 'ROLE_MANAGER']);
      expect(result.current.user?.displayName).toBe('Test User');
    });
    expect(localStorage.getItem('ticket-auth')).toContain('"token":"test-token"');
  });

  it('clears state and storage on logout', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    act(() => {
      result.current.login({
        token: 'abc',
        userId: '1',
        email: 'test@example.com',
        displayName: 'Test',
        roles: ['ROLE_USER'],
      });
    });

    await waitFor(() => expect(result.current.token).toBe('abc'));

    act(() => {
      result.current.logout();
    });

    expect(result.current.token).toBeNull();
    expect(result.current.user).toBeNull();
    expect(localStorage.getItem('ticket-auth')).toBeNull();
  });

  it('fetches profile when a token exists in storage', async () => {
    localStorage.setItem(
      'ticket-auth',
      JSON.stringify({ token: 'stored-token', user: null, roles: [] })
    );

    apiRequest.mockResolvedValue({
      id: '22222222-2222-2222-2222-222222222222',
      email: 'profile@example.com',
      displayName: 'Profile Name',
      roles: ['ROLE_ADMIN'],
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => {
      expect(apiRequest).toHaveBeenCalledWith('/me', {}, 'stored-token');
    });

    await waitFor(() => {
      expect(result.current.user?.email).toBe('profile@example.com');
      expect(result.current.roles).toEqual(['ROLE_ADMIN']);
      expect(result.current.error).toBeNull();
    });
  });

  it('sets an error when profile fetch fails', async () => {
    localStorage.setItem(
      'ticket-auth',
      JSON.stringify({ token: 'bad-token', user: null, roles: [] })
    );

    apiRequest.mockRejectedValue(new Error('Boom'));

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => {
      expect(result.current.error).toBe('Failed to fetch profile');
    });
  });
});
