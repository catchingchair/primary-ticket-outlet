import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ManagerDashboard from './ManagerDashboard';

const useAuthMock = vi.fn();

vi.mock('../../auth', () => ({
  useAuth: () => useAuthMock(),
}));

vi.mock('../../../api/client', () => ({
  apiRequest: vi.fn(),
  downloadCsv: vi.fn(),
}));

describe('ManagerDashboard', () => {
  beforeEach(() => {
    useAuthMock.mockReset();
  });

  it('shows info alert when user manages no venues', () => {
    useAuthMock.mockReturnValue({
      token: 'token',
      user: { managedVenues: [] },
    });

    render(<ManagerDashboard />);

    expect(
      screen.getByText(/No venues assigned\. Contact an administrator/i)
    ).toBeInTheDocument();
  });
});
