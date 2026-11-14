-- Existing users without a settings row get the defaults new accounts are created with
-- (EUR, no custom currency, MONTH), matching UserSettingsDto and the mobile app.
INSERT INTO user_settings (user_id, currency, custom_currency, tool_basis, updated_at)
SELECT id, 'EUR', '', 'MONTH', (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
FROM users
ON CONFLICT (user_id) DO NOTHING;
