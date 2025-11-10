INSERT INTO venues (id, name, location, description)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Downtown Theater', 'New York, NY', 'Historic theater in downtown Manhattan'),
    ('22222222-2222-2222-2222-222222222222', 'Bayview Pavilion', 'San Francisco, CA', 'Waterfront venue with skyline views')
ON CONFLICT (id) DO NOTHING;

INSERT INTO events (id, venue_id, title, description, starts_at, ends_at, face_value_cents)
VALUES
    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
     'Winter Jazz Night', 'An evening with top jazz performers.',
     NOW() + INTERVAL '14 day',
     NOW() + INTERVAL '14 day' + INTERVAL '3 hour',
     8500),
    ('44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222',
     'Tech Innovators Summit', 'Talks and panels on the future of technology.',
     NOW() + INTERVAL '30 day',
     NOW() + INTERVAL '30 day' + INTERVAL '6 hour',
     12500)
ON CONFLICT (id) DO NOTHING;
