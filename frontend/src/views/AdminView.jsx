import {
  Alert,
  Card,
  CardContent,
  CardHeader,
  Stack,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import { useAuth } from '../context/AuthContext';

function formatCurrency(cents) {
  return Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(cents / 100);
}

export default function AdminView() {
  const { token } = useAuth();
  const [venues, setVenues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      if (!token) return;
      setLoading(true);
      try {
        const response = await apiRequest('/admin/dashboard', {}, token);
        if (!cancelled) {
          setVenues(response ?? []);
          setError(null);
        }
      } catch (err) {
        if (!cancelled) {
          console.error(err);
          setError(err.message);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [token]);

  if (loading) {
    return <Typography>Loading admin data…</Typography>;
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (!venues.length) {
    return (
      <Alert severity="info">
        No venues found. Create venues through the manager interface first.
      </Alert>
    );
  }

  return (
    <Stack spacing={3}>
      <Typography variant="h5" component="h2">
        Venues
      </Typography>
      <Grid container spacing={2}>
      {venues.map((venue) => (
        <Grid key={venue.id} item xs={12} md={6}>
          <Card variant="outlined">
            <CardHeader
              title={venue.name}
              subheader={venue.location || 'Location not provided'}
              titleTypographyProps={{ component: 'h3', variant: 'h6' }}
              subheaderTypographyProps={{ component: 'p' }}
            />
            <CardContent>
              <Stack spacing={1}>
                {venue.events.map((event) => (
                  <Stack
                    key={event.id}
                    sx={{
                      border: '1px solid',
                      borderColor: 'divider',
                      borderRadius: 1,
                      p: 2,
                    }}
                    spacing={0.5}
                  >
                    <Typography variant="subtitle1">{event.title}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      Starts {new Date(event.startsAt).toLocaleString()}
                    </Typography>
                    <Typography variant="body2">
                      Tickets sold: {event.ticketsSold} / {event.ticketsTotal}
                    </Typography>
                    <Typography variant="body2">
                      Revenue: {formatCurrency(event.revenueCents)}
                    </Typography>
                  </Stack>
                ))}
                {!venue.events.length && (
                  <Typography color="text.secondary">
                    No events scheduled for this venue.
                  </Typography>
                )}
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      ))}
      </Grid>
    </Stack>
  );
}
