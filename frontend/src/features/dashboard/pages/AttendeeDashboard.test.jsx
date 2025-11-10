import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import AttendeeDashboard from './AttendeeDashboard';

const apiRequestMock = vi.fn();
const useAuthMock = vi.fn();
let consoleErrorSpy;

vi.mock('../../../api/client', () => ({
  apiRequest: (...args) => apiRequestMock(...args),
  ApiError: class ApiError extends Error {},
}));

vi.mock('../../auth', () => ({
  useAuth: () => useAuthMock(),
}));

describe('AttendeeDashboard', () => {
  beforeEach(() => {
    apiRequestMock.mockReset();
    useAuthMock.mockReturnValue({ token: 'test-token' });
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    consoleErrorSpy?.mockRestore();
  });

  it('loads events and completes a purchase', async () => {
    const events = [
      {
        id: 'event-1',
        title: 'Jazz Night',
        description: 'Great music',
        venueName: 'Downtown Theater',
        startsAt: new Date().toISOString(),
        faceValueCents: 8500,
        ticketsSold: 0,
        ticketsTotal: 100,
      },
    ];

    apiRequestMock.mockImplementation(async (path, options, token) => {
      if (path === '/events') {
        expect(token).toBe('test-token');
        return events;
      }
      if (path === '/events/event-1/purchase') {
        expect(options.method).toBe('POST');
        return { ticketCodes: ['CODE123'] };
      }
      throw new Error(`Unexpected path ${path}`);
    });

    const user = userEvent.setup();

    render(<AttendeeDashboard />);

    await waitFor(() => {
      expect(screen.getByText('Jazz Night')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Purchase' }));

    await waitFor(() => {
      expect(screen.getByText(/Purchase confirmed/i)).toBeInTheDocument();
      expect(apiRequestMock).toHaveBeenCalledWith(
        '/events/event-1/purchase',
        expect.objectContaining({
          method: 'POST',
          data: expect.objectContaining({ quantity: 1 }),
        }),
        'test-token'
      );
    });
  });

  it('shows an error when loading events fails', async () => {
    apiRequestMock.mockRejectedValueOnce(new Error('Network unreachable'));

    render(<AttendeeDashboard />);

    await waitFor(() => {
      expect(screen.getByText('Failed to load events')).toBeInTheDocument();
    });
  });
});
