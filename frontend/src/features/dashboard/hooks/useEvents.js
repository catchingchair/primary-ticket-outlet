import { useEffect, useState } from 'react';
import { apiRequest } from '../../../api/client';

export function useEvents(token) {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!token) {
      setEvents([]);
      setLoading(false);
      return;
    }

    let cancelled = false;
    async function loadEvents() {
      setLoading(true);
      try {
        const data = await apiRequest('/events', {}, token);
        if (!cancelled) {
          setEvents(data ?? []);
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

    loadEvents();
    return () => {
      cancelled = true;
    };
  }, [token]);

  return { events, loading, error };
}

