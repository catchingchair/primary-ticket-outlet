import {
  Chip,
  Stack,
  ToggleButton,
  ToggleButtonGroup,
  Tooltip,
  Typography,
} from '@mui/material';

const ROLE_LABELS = {
  ROLE_USER: 'Attendee',
  ROLE_MANAGER: 'Manager',
  ROLE_ADMIN: 'Admin',
};

export default function RoleSwitcher({ roles, activeRole, onRoleChange }) {
  if (!roles?.length) {
    return null;
  }

  return (
    <Stack direction="row" spacing={2} alignItems="center">
      <Typography variant="body2" color="text.secondary">
        View as:
      </Typography>
      <ToggleButtonGroup
        color="primary"
        value={activeRole}
        exclusive
        onChange={(event, value) => value && onRoleChange(value)}
      >
        {roles.map((role) => (
          <ToggleButton key={role} value={role}>
            {ROLE_LABELS[role] ?? role}
          </ToggleButton>
        ))}
      </ToggleButtonGroup>
      <Tooltip title="Active roles for this session">
        <Stack direction="row" spacing={1}>
          {roles.map((role) => (
            <Chip
              key={role}
              label={ROLE_LABELS[role] ?? role}
              size="small"
              color={role === activeRole ? 'primary' : 'default'}
            />
          ))}
        </Stack>
      </Tooltip>
    </Stack>
  );
}

