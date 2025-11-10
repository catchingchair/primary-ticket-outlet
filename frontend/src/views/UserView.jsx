import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useEffect, useMemo, useState } from 'react';
import { apiRequest, ApiError } from '../api/client';
import { useAuth } from '../context/AuthContext';

function formatCurrency(cents) {
  return Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(cents / 100);
}

export default function UserView() {
  const { token } = useAuth();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [purchaseState, setPurchaseState] = useState({});

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      try {
        const response = await apiRequest('/events', {}, token);
        if (!cancelled) {
          setEvents(response ?? []);
          setError(null);
        }
      } catch (err) {
        if (!cancelled) {
          console.error(err);
          setError('Failed to load events');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }
    if (token) {
      load();
    }
    return () => {
      cancelled = true;
    };
  }, [token]);

  const handlePurchaseChange = (eventId, patch) => {
    setPurchaseState((prev) => ({
      ...prev,
      [eventId]: {
        quantity: 1,
        paymentToken: 'demo-token',
        ...prev[eventId],
        ...patch,
      },
    }));
  };

  const handlePurchase = async (eventId) => {
    const purchaseInput = purchaseState[eventId] ?? {
      quantity: 1,
      paymentToken: 'demo-token',
    };
    try {
      const idempotencyKey = crypto.randomUUID();
      const result = await apiRequest(
        `/events/${eventId}/purchase`,
        {
          method: 'POST',
          data: {
            quantity: Number(purchaseInput.quantity),
            paymentToken: purchaseInput.paymentToken || 'demo-token',
          },
          headers: {
            'Idempotency-Key': idempotencyKey,
          },
        },
        token
      );
      setPurchaseState((prev) => ({
        ...prev,
        [eventId]: {
          ...purchaseInput,
          lastResult: result,
          error: null,
        },
      }));
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Unexpected error';
      setPurchaseState((prev) => ({
        ...prev,
        [eventId]: {
          ...purchaseInput,
          error: message,
        },
      }));
    }
  };

  const sortedEvents = useMemo(
    () =>
      [...events].sort(
        (a, b) => new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime()
      ),
    [events]
  );

  if (loading) {
    return <Typography>Loading events…</Typography>;
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (!sortedEvents.length) {
    return <Typography>No upcoming events yet. Check back soon!</Typography>;
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h5" gutterBottom>
          Upcoming Events
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Reserve seats, view details, and complete purchases with your token.
        </Typography>
      </Box>
      <Grid container spacing={2}>
      {sortedEvents.map((event) => {
        const state = purchaseState[event.id] ?? {
          quantity: 1,
          paymentToken: 'demo-token',
        };
        return (
          <Grid key={event.id} item xs={12} md={6}>
            <Card variant="outlined">
              <CardHeader
                title={event.title}
                subheader={`${event.venueName} • ${new Date(
                  event.startsAt
                ).toLocaleString()}`}
              />
              <CardContent>
                <Typography variant="body1" gutterBottom>
                  {event.description || 'No description provided.'}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Ticket price: {formatCurrency(event.faceValueCents)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Tickets sold: {event.ticketsSold} / {event.ticketsTotal}
                </Typography>
                {state.lastResult && (
                  <Alert severity="success" sx={{ mt: 2 }}>
                    Purchase confirmed. Ticket codes:{' '}
                    {state.lastResult.ticketCodes.join(', ')}
                  </Alert>
                )}
                {state.error && (
                  <Alert severity="error" sx={{ mt: 2 }}>
                    {state.error}
                  </Alert>
                )}
              </CardContent>
              <CardActions>
                <Box
                  component="form"
                  onSubmit={(eventSubmit) => {
                    eventSubmit.preventDefault();
                    handlePurchase(event.id);
                  }}
                  sx={{ width: '100%' }}
                >
                  <Stack direction="row" spacing={2}>
                    <TextField
                      label="Quantity"
                      type="number"
                      size="small"
                      value={state.quantity}
                      onChange={(eventChange) =>
                        handlePurchaseChange(event.id, {
                          quantity: eventChange.target.value,
                        })
                      }
                      inputProps={{ min: 1, max: 10 }}
                    />
                    <TextField
                      label="Payment Token"
                      size="small"
                      value={state.paymentToken}
                      onChange={(eventChange) =>
                        handlePurchaseChange(event.id, {
                          paymentToken: eventChange.target.value,
                        })
                      }
                    />
                    <Button
                      type="submit"
                      variant="contained"
                      sx={{ whiteSpace: 'nowrap' }}
                    >
                      Purchase
                    </Button>
                  </Stack>
                </Box>
              </CardActions>
            </Card>
          </Grid>
        );
      })}
      </Grid>
    </Stack>
  );
}
