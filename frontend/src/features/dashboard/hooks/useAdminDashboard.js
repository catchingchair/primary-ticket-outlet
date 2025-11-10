import { useEffect, useState } from 'react';
import { apiRequest } from '../../../api/client';

export function useAdminDashboard(token) {
  const [venues, setVenues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!token) {
      setVenues([]);
      setLoading(false);
      return;
    }

    let cancelled = false;
    async function load() {
      setLoading(true);
      try {
        const result = await apiRequest('/admin/dashboard', {}, token);
        if (!cancelled) {
          setVenues(result ?? []);
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

  return { venues, loading, error };
}

