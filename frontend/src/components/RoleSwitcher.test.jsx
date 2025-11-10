import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import RoleSwitcher from './RoleSwitcher';

describe('RoleSwitcher', () => {
  it('returns null when no roles provided', () => {
    const { container } = render(
      <RoleSwitcher roles={[]} activeRole="ROLE_USER" onRoleChange={() => {}} />
    );

    expect(container).toBeEmptyDOMElement();
  });

  it('renders a toggle button for each role', () => {
    render(
      <RoleSwitcher
        roles={['ROLE_USER', 'ROLE_MANAGER']}
        activeRole="ROLE_USER"
        onRoleChange={() => {}}
      />
    );

    expect(screen.getByRole('button', { name: 'Attendee' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Manager' })).toBeInTheDocument();
    expect(screen.getByRole('group')).toBeInTheDocument();
  });

  it('invokes onRoleChange when a different role is selected', async () => {
    const onRoleChange = vi.fn();
    const user = userEvent.setup();

    render(
      <RoleSwitcher
        roles={['ROLE_USER', 'ROLE_ADMIN']}
        activeRole="ROLE_USER"
        onRoleChange={onRoleChange}
      />
    );

    await user.click(screen.getByRole('button', { name: 'Admin' }));
    expect(onRoleChange).toHaveBeenCalledWith('ROLE_ADMIN');
  });
});
