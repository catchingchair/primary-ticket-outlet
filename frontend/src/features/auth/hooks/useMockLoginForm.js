import { useCallback, useMemo, useState } from 'react';

export function useMockLoginForm() {
  const [formState, setFormState] = useState({
    email: '',
    displayName: '',
    asManager: false,
    asAdmin: false,
    managedVenuesRaw: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const roles = useMemo(() => {
    const values = ['ROLE_USER'];
    if (formState.asManager) values.push('ROLE_MANAGER');
    if (formState.asAdmin) values.push('ROLE_ADMIN');
    return values;
  }, [formState.asManager, formState.asAdmin]);

  const managedVenueIds = useMemo(
    () =>
      formState.managedVenuesRaw
        .split(',')
        .map((value) => value.trim())
        .filter(Boolean),
    [formState.managedVenuesRaw]
  );

  const updateField = useCallback((key, value) => {
    setFormState((prev) => ({ ...prev, [key]: value }));
  }, []);

  const resetErrors = useCallback(() => setError(null), []);

  return {
    formState,
    roles,
    managedVenueIds,
    submitting,
    setSubmitting,
    error,
    setError,
    updateField,
    resetErrors,
  };
}

