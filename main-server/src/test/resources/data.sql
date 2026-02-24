ALTER TABLE IF EXISTS users
    DROP CONSTRAINT IF EXISTS fk_user_created_by;
ALTER TABLE IF EXISTS users
    DROP CONSTRAINT IF EXISTS fk_user_last_updated_by;
ALTER TABLE IF EXISTS department
    DROP CONSTRAINT IF EXISTS fk_department_created_by;
ALTER TABLE IF EXISTS department
    DROP CONSTRAINT IF EXISTS fk_department_last_updated_by;

INSERT INTO department
VALUES
('d1d11111-11d1-1d11-1111-d111111d1111', 'test_admin_department', true,
'u1u11111-11u1-1u11-1111-u111111u1111', '2025-09-29 13:00:00',
'u1u11111-11u1-1u11-1111-u111111u1111', '2025-09-29 13:00:00');

INSERT INTO users
VALUES
('u1u11111-11u1-1u11-1111-u111111u1111', 'test_admin', 'some_password', true,
'd1d11111-11d1-1d11-1111-d111111d1111', 'ADMIN',
'u1u11111-11u1-1u11-1111-u111111u1111', '2025-09-29 13:00:00',
'u1u11111-11u1-1u11-1111-u111111u1111', '2025-09-29 13:00:00');

ALTER TABLE department
    ADD CONSTRAINT fk_department_created_by FOREIGN KEY (created_by)
        REFERENCES users (id);
ALTER TABLE department
    ADD CONSTRAINT fk_department_last_updated_by FOREIGN KEY (last_updated_by)
        REFERENCES users (id);
ALTER TABLE users
    ADD CONSTRAINT fk_user_created_by FOREIGN KEY (created_by)
        REFERENCES users (id);
ALTER TABLE users
    ADD CONSTRAINT fk_user_last_updated_by FOREIGN KEY (last_updated_by)
        REFERENCES users (id);