INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_MANAGER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;

INSERT INTO users (id, email, display_name, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin@example.com', 'Admin', NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@example.com'
  AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;
