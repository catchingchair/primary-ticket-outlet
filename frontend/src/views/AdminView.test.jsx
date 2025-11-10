import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AdminView from './AdminView';

const apiRequestMock = vi.fn();
const useAuthMock = vi.fn();

vi.mock('../api/client', () => ({
  apiRequest: (...args) => apiRequestMock(...args),
}));

vi.mock('../context/AuthContext', () => ({
  useAuth: () => useAuthMock(),
}));

describe('AdminView', () => {
  beforeEach(() => {
    apiRequestMock.mockReset();
    useAuthMock.mockReturnValue({ token: 'token' });
  });

  it('renders venue cards after data loads', async () => {
    apiRequestMock.mockResolvedValue([
      {
        id: 'venue-1',
        name: 'Downtown Theater',
        location: 'NYC',
        events: [
          {
            id: 'event-1',
            title: 'Jazz Night',
            startsAt: new Date().toISOString(),
            ticketsSold: 10,
            ticketsTotal: 100,
            revenueCents: 85000,
          },
        ],
      },
    ]);

    render(<AdminView />);

    await waitFor(() => {
      expect(screen.getByText('Downtown Theater')).toBeInTheDocument();
      expect(screen.getByText(/Jazz Night/)).toBeInTheDocument();
    });
  });

  it('shows info alert when no venues are returned', async () => {
    apiRequestMock.mockResolvedValue([]);

    render(<AdminView />);

    await waitFor(() => {
      expect(
        screen.getByText(/No venues found\. Create venues/)
      ).toBeInTheDocument();
    });
  });
});

