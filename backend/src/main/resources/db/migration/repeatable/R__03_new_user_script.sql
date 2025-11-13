-- insert a new user with all three roles.
INSERT INTO users (id, email, display_name, created_at)
VALUES ('00000000-0000-0000-0000-000000000002', 'NewUser@example.com', 'NewUser', NOW())
ON CONFLICT (email) DO NOTHING;

-- insert into the user roles the new role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'NewUser@example.com'
  AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'NewUser@example.com'
  AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'NewUser@example.com'
  AND r.name = 'ROLE_MANAGER'
ON CONFLICT DO NOTHING;

-- update the event for the manager role to be the Downtown Theater Venue
UPDATE user_roles SET venue_id = '11111111-1111-1111-1111-111111111111' 
WHERE role_id = (Select id from roles WHERE name = 'ROLE_MANAGER');
