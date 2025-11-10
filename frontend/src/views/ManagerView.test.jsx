import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ManagerView from './ManagerView';

const useAuthMock = vi.fn();

vi.mock('../context/AuthContext', () => ({
  useAuth: () => useAuthMock(),
}));

vi.mock('../api/client', () => ({
  apiRequest: vi.fn(),
  downloadCsv: vi.fn(),
}));

describe('ManagerView', () => {
  beforeEach(() => {
    useAuthMock.mockReset();
  });

  it('shows info alert when user manages no venues', () => {
    useAuthMock.mockReturnValue({
      token: 'token',
      user: { managedVenues: [] },
    });

    render(<ManagerView />);

    expect(
      screen.getByText(/No venues assigned\. Contact an administrator/i)
    ).toBeInTheDocument();
  });
});

