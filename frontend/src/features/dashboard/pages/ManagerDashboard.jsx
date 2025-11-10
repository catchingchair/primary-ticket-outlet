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
import { apiRequest } from '../../../api/client';
import { useAuth } from '../../auth';
import { useManagerVenues } from '../hooks/useManagerVenues';

function defaultDate(hoursAhead) {
  const date = new Date(Date.now() + hoursAhead * 60 * 60 * 1000);
  date.setMinutes(0, 0, 0);
  return date.toISOString().slice(0, 16);
}

export default function ManagerDashboard() {
  const { token, user } = useAuth();
  const managedVenues = useMemo(
    () => user?.managedVenues ?? [],
    [user?.managedVenues]
  );
  const [form, setForm] = useState(() => ({
    venueId: managedVenues[0]?.id ?? '',
    title: '',
    description: '',
    startsAt: defaultDate(48),
    endsAt: defaultDate(52),
    faceValueCents: 7500,
  }));
  const {
    eventsByVenue,
    loading,
    error,
    successMessage,
    setError,
    setSuccessMessage,
    generateTickets,
    downloadPurchasers,
    refresh,
  } = useManagerVenues(token, managedVenues);

  useEffect(() => {
    setForm((prev) => ({
      ...prev,
      venueId: managedVenues[0]?.id ?? '',
    }));
  }, [managedVenues]);

  const handleChange = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleCreateEvent = async (event) => {
    event.preventDefault();
    setSuccessMessage(null);
    try {
      await apiRequest(
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
      await refresh();
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
      await generateTickets(eventId, venueId, quantity);
    } catch (err) {
      console.error(err);
      setError(err.message);
    }
  };

  const handleDownloadPurchasers = async (eventId) => {
    try {
      await downloadPurchasers(eventId);
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
