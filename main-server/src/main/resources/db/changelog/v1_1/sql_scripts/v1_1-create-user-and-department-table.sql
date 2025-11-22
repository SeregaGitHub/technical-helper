CREATE TYPE user_role AS ENUM ('ADMIN', 'TECHNICIAN', 'EMPLOYEE');

GO

CREATE TABLE IF NOT EXISTS department (
  id                varchar(36) NOT NULL,
  name              varchar(64) NOT NULL,
  enabled           boolean     NOT NULL,
  created_by        varchar(36) NOT NULL,
  created_date      timestamp   NOT NULL,
  last_updated_by   varchar(36) NOT NULL,
  last_updated_date timestamp   NOT NULL,
  CONSTRAINT pk_department_id              PRIMARY KEY (id),
  CONSTRAINT uk_department_name            UNIQUE (name)
)

GO

CREATE TABLE IF NOT EXISTS users (
  id                varchar(36)  NOT NULL,
  username          varchar(64)  NOT NULL,
  password          varchar(128) NOT NULL,
  enabled           boolean      NOT NULL,
  department        varchar(36)  NOT NULL,
  role              user_role    NOT NULL,
  created_by        varchar(36)  NOT NULL,
  created_date      timestamp    NOT NULL,
  last_updated_by   varchar(36)  NOT NULL,
  last_updated_date timestamp    NOT NULL,
  CONSTRAINT pk_users_id             PRIMARY KEY (id),
  CONSTRAINT uk_users_username       UNIQUE (username),
  CONSTRAINT fk_users_department     FOREIGN KEY (department)
        REFERENCES department (id)
)

GO

ALTER TABLE department
	ADD CONSTRAINT fk_department_created_by FOREIGN KEY (created_by)
		REFERENCES users (id)

GO

ALTER TABLE department
	ADD CONSTRAINT fk_department_last_updated_by FOREIGN KEY (last_updated_by)
		REFERENCES users (id)

GO

ALTER TABLE users
	ADD CONSTRAINT fk_user_created_by FOREIGN KEY (created_by)
		REFERENCES users (id)

GO

ALTER TABLE users
	ADD CONSTRAINT fk_user_last_updated_by FOREIGN KEY (last_updated_by)
		REFERENCES users (id)

GO

CREATE PROCEDURE drop_constraints()
LANGUAGE plpgsql
AS
'
BEGIN
	ALTER TABLE IF EXISTS users
		DROP CONSTRAINT IF EXISTS fk_user_created_by;
	ALTER TABLE IF EXISTS users
		DROP CONSTRAINT IF EXISTS fk_user_last_updated_by;
	ALTER TABLE IF EXISTS department
		DROP CONSTRAINT IF EXISTS fk_department_created_by;
	ALTER TABLE IF EXISTS department
		DROP CONSTRAINT IF EXISTS fk_department_last_updated_by;
END; '

GO

CREATE PROCEDURE set_current_id(
	IN dep_id varchar, IN adm_id varchar,
	IN dep_created_by varchar, IN adm_created_by varchar,
	IN dep_last_updated_by varchar, IN adm_last_updated_by varchar,
	IN temporary_admin_id varchar
)
LANGUAGE plpgsql
AS
'
BEGIN

	IF dep_created_by = temporary_admin_id THEN
		UPDATE department
		SET created_by = adm_id
		WHERE id = dep_id;
	END IF;
	IF dep_last_updated_by = temporary_admin_id THEN
		UPDATE department
		SET last_updated_by = adm_id
		WHERE id = dep_id;
	END IF;
	IF adm_created_by = temporary_admin_id THEN
		UPDATE users
		SET created_by = adm_id
		WHERE id = adm_id;
	END IF;

	UPDATE users
	SET last_updated_by = adm_id
	WHERE id = adm_id;

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

END; '

GO

CREATE FUNCTION delete_all_users_from_department() RETURNS trigger AS
'
BEGIN
	UPDATE users
		SET enabled = false
		WHERE department = OLD.id;
	RETURN OLD;
END;
' LANGUAGE plpgsql;

GO

CREATE TRIGGER delete_all_users_from_department AFTER UPDATE OF enabled ON department
FOR EACH ROW EXECUTE PROCEDURE delete_all_users_from_department()

GO
