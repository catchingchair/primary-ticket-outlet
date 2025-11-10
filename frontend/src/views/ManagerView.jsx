import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  CardHeader,
  Divider,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useEffect, useMemo, useState } from 'react';
import { apiRequest, downloadCsv } from '../api/client';
import { useAuth } from '../context/AuthContext';

function defaultDate(hoursAhead) {
  const date = new Date(Date.now() + hoursAhead * 60 * 60 * 1000);
  date.setMinutes(0, 0, 0);
  return date.toISOString().slice(0, 16);
}

export default function ManagerView() {
  const { token, user } = useAuth();
  const managedVenues = user?.managedVenues ?? [];
  const [eventsByVenue, setEventsByVenue] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [form, setForm] = useState(() => ({
    venueId: managedVenues[0]?.id ?? '',
    title: '',
    description: '',
    startsAt: defaultDate(48),
    endsAt: defaultDate(52),
    faceValueCents: 7500,
  }));

  useEffect(() => {
    setForm((prev) => ({
      ...prev,
      venueId: managedVenues[0]?.id ?? '',
    }));
  }, [managedVenues]);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      if (!managedVenues.length || !token) {
        setLoading(false);
        return;
      }
      setLoading(true);
      try {
        const results = {};
        for (const venue of managedVenues) {
          const venueEvents = await apiRequest(
            `/venues/${venue.id}/events`,
            {},
            token
          );
          results[venue.id] = venueEvents ?? [];
        }
        if (!cancelled) {
          setEventsByVenue(results);
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
    load();
    return () => {
      cancelled = true;
    };
  }, [managedVenues, token]);

  const handleChange = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleCreateEvent = async (event) => {
    event.preventDefault();
    setSuccessMessage(null);
    try {
      const created = await apiRequest(
        `/venues/${form.venueId}/events`,
        {
          method: 'POST',
          data: {
            title: form.title,
            description: form.description,
            startsAt: new Date(form.startsAt).toISOString(),
            endsAt: new Date(form.endsAt).toISOString(),
            faceValueCents: Number(form.faceValueCents),
          },
        },
        token
      );
      setEventsByVenue((prev) => ({
        ...prev,
        [form.venueId]: [...(prev[form.venueId] ?? []), created],
      }));
      setForm((prev) => ({
        ...prev,
        title: '',
        description: '',
      }));
      setSuccessMessage('Event created successfully.');
    } catch (err) {
      console.error(err);
      setError(err.message);
    }
  };

  const handleGenerateTickets = async (eventId, venueId) => {
    const quantityRaw = window.prompt(
      'How many tickets should be generated?',
      '50'
    );
    if (!quantityRaw) return;
    const quantity = Number(quantityRaw);
    if (Number.isNaN(quantity) || quantity <= 0) {
      setError('Please enter a valid ticket quantity.');
      return;
    }
    try {
      await apiRequest(
        `/events/${eventId}/tickets:generate`,
        {
          method: 'POST',
          data: { quantity },
        },
        token
      );
      const refreshedEvents = await apiRequest(
        `/venues/${venueId}/events`,
        {},
        token
      );
      setEventsByVenue((prev) => ({
        ...prev,
        [venueId]: refreshedEvents ?? [],
      }));
      setSuccessMessage(`Generated ${quantity} tickets.`);
    } catch (err) {
      console.error(err);
      setError(err.message);
    }
  };

  const handleDownloadPurchasers = async (eventId) => {
    try {
      await downloadCsv(`/events/${eventId}/purchasers`, token);
    } catch (err) {
      console.error(err);
      setError(err.message);
    }
  };

  const allEvents = useMemo(() => {
    const list = [];
    Object.entries(eventsByVenue).forEach(([venueId, events]) => {
      events.forEach((eventItem) => {
        list.push({ ...eventItem, venueId });
      });
    });
    return list;
  }, [eventsByVenue]);

  if (!managedVenues.length) {
    return (
      <Alert severity="info">
        No venues assigned. Contact an administrator to grant venue manager
        access.
      </Alert>
    );
  }

  return (
    <Stack spacing={3}>
      {error && <Alert severity="error">{error}</Alert>}
      {successMessage && <Alert severity="success">{successMessage}</Alert>}

      <Card variant="outlined">
        <CardHeader
          title="Create Event"
          titleTypographyProps={{ component: 'h2', variant: 'h5' }}
        />
        <CardContent>
          <Box component="form" onSubmit={handleCreateEvent}>
            <Stack spacing={2}>
              <TextField
                label="Venue"
                select
                value={form.venueId}
                onChange={(eventChange) =>
                  handleChange('venueId', eventChange.target.value)
                }
                required
              >
                {managedVenues.map((venue) => (
                  <MenuItem key={venue.id} value={venue.id}>
                    {venue.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Title"
                value={form.title}
                onChange={(eventChange) =>
                  handleChange('title', eventChange.target.value)
                }
                required
              />
              <TextField
                label="Description"
                multiline
                minRows={3}
                value={form.description}
                onChange={(eventChange) =>
                  handleChange('description', eventChange.target.value)
                }
              />
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <TextField
                  label="Starts At"
                  type="datetime-local"
                  value={form.startsAt}
                  onChange={(eventChange) =>
                    handleChange('startsAt', eventChange.target.value)
                  }
                  InputLabelProps={{ shrink: true }}
                  required
                  fullWidth
                />
                <TextField
                  label="Ends At"
                  type="datetime-local"
                  value={form.endsAt}
                  onChange={(eventChange) =>
                    handleChange('endsAt', eventChange.target.value)
                  }
                  InputLabelProps={{ shrink: true }}
                  required
                  fullWidth
                />
              </Stack>
              <TextField
                label="Face Value (cents)"
                type="number"
                value={form.faceValueCents}
                onChange={(eventChange) =>
                  handleChange('faceValueCents', eventChange.target.value)
                }
                required
              />
              <Button variant="contained" type="submit">
                Create Event
              </Button>
            </Stack>
          </Box>
        </CardContent>
      </Card>

      <Divider />

      <Typography variant="h6">
        Upcoming Events ({allEvents.length})
      </Typography>
      {loading ? (
        <Typography>Loading events…</Typography>
      ) : (
        <Grid container spacing={2}>
          {managedVenues.map((venue) => (
            <Grid key={venue.id} item xs={12} md={6}>
              <Card variant="outlined">
                <CardHeader
                  title={venue.name}
                  subheader={venue.location || 'Location TBA'}
                  titleTypographyProps={{ component: 'h3', variant: 'h6' }}
                  subheaderTypographyProps={{ component: 'p' }}
                />
                <CardContent>
                  <Stack spacing={2}>
                    {(eventsByVenue[venue.id] ?? []).map((eventItem) => (
                      <Box
                        key={eventItem.id}
                        sx={{
                          border: '1px solid',
                          borderColor: 'divider',
                          borderRadius: 1,
                          p: 2,
                        }}
                      >
                        <Typography variant="subtitle1">
                          {eventItem.title}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {new Date(
                            eventItem.startsAt
                          ).toLocaleString()} • Tickets:{' '}
                          {eventItem.ticketsSold} / {eventItem.ticketsTotal}
                        </Typography>
                        <Typography variant="body2">
                          {eventItem.description || 'No description provided.'}
                        </Typography>
                        <CardActions sx={{ px: 0 }}>
                          <Button
                            size="small"
                            onClick={() =>
                              handleGenerateTickets(eventItem.id, venue.id)
                            }
                          >
                            Generate Tickets
                          </Button>
                          <Button
                            size="small"
                            onClick={() => handleDownloadPurchasers(eventItem.id)}
                          >
                            Download Purchasers CSV
                          </Button>
                        </CardActions>
                      </Box>
                    ))}
                    {!eventsByVenue[venue.id]?.length && (
                      <Typography color="text.secondary">
                        No events yet for this venue.
                      </Typography>
                    )}
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Stack>
  );
}
