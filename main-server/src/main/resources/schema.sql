--------------------- only for develop mode
--DROP TABLE IF EXISTS breakage_comment_audit;
--DROP TABLE IF EXISTS breakage_comment;
--DROP TABLE IF EXISTS breakage_audit;
--DROP TABLE IF EXISTS breakage;
--DROP TABLE IF EXISTS users;
--DROP TABLE IF EXISTS department;
--------------------- only for develop mode

ALTER TABLE IF EXISTS users
	DROP CONSTRAINT IF EXISTS fk_user_created_by;

ALTER TABLE IF EXISTS users
	DROP CONSTRAINT IF EXISTS fk_user_last_updated_by;

ALTER TABLE IF EXISTS department
	DROP CONSTRAINT IF EXISTS fk_department_created_by;

ALTER TABLE IF EXISTS department
	DROP CONSTRAINT IF EXISTS fk_department_last_updated_by;


CREATE TABLE IF NOT EXISTS department (
  id                varchar(36) NOT NULL,
  name              varchar(64) NOT NULL,
  enabled           boolean     NOT NULL,
  created_by        varchar(36) NOT NULL,
  created_date      timestamp   NOT NULL,
  last_updated_by   varchar(36) NOT NULL,
  last_updated_date timestamp   NOT NULL,
  CONSTRAINT pk_department_id   PRIMARY KEY (id),
  CONSTRAINT uk_department_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS users (
  id                varchar(36)  NOT NULL,
  username          varchar(64)  NOT NULL,
  password          varchar(128) NOT NULL,
  enabled           boolean      NOT NULL,
  department        varchar(36)  NOT NULL,
  role              varchar(15)  NOT NULL,
  created_by        varchar(36)  NOT NULL,
  created_date      timestamp    NOT NULL,
  last_updated_by   varchar(36)  NOT NULL,
  last_updated_date timestamp    NOT NULL,
  CONSTRAINT pk_users_id         PRIMARY KEY (id),
  CONSTRAINT uk_users_username   UNIQUE (username),
  CONSTRAINT ch_users_role       CHECK (role IN ('ADMIN', 'TECHNICIAN', 'EMPLOYEE')),
  CONSTRAINT fk_users_department FOREIGN KEY (department)
        REFERENCES department (id)
);

CREATE TABLE IF NOT EXISTS breakage (
  id                         varchar(36)   NOT NULL,
  department                 varchar(36)   NOT NULL,
  room                       varchar(128)  NOT NULL,
  breakage_topic             varchar(64)   NOT NULL,
  breakage_text              varchar(2048) NOT NULL,
  status                     varchar(11)   NOT NULL,
  priority                   varchar(8)    NOT NULL,
  executor                   varchar(36),
  executor_appointed_by      varchar(36),
  deadline                   timestamp,
  created_by                 varchar(36)   NOT NULL,
  created_date               timestamp     NOT NULL,
  last_updated_by            varchar(36)   NOT NULL,
  last_updated_date          timestamp     NOT NULL,
  CONSTRAINT pk_breakage_id                    PRIMARY KEY (id),
  CONSTRAINT fk_breakage_department            FOREIGN KEY (department)
        REFERENCES department (id),
  CONSTRAINT ch_breakage_status                CHECK (status IN (
                                                        'NEW', 'SOLVED', 'IN_PROGRESS',
                                                        'PAUSED', 'REDIRECTED', 'CANCELLED')
                                                        ),
  CONSTRAINT ch_breakage_priority              CHECK (priority IN ('URGENTLY', 'HIGH', 'MEDIUM', 'LOW')),
  CONSTRAINT fk_breakage_executor              FOREIGN KEY (executor)
        REFERENCES users (id),
  CONSTRAINT fk_breakage_executor_appointed_by FOREIGN KEY (executor_appointed_by)
        REFERENCES users (id),
  CONSTRAINT fk_breakage_created_by            FOREIGN KEY (created_by)
        REFERENCES users (id),
  CONSTRAINT fk_breakage_last_updated_by       FOREIGN KEY (last_updated_by)
        REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS breakage_audit (
  operation                  char(1)       NOT NULL,
  breakage                   varchar(36)   NOT NULL,
  department                 varchar(36)   NOT NULL,
  room                       varchar(128)  NOT NULL,
  breakage_topic             varchar(64)   NOT NULL,
  breakage_text              varchar(2048) NOT NULL,
  status                     varchar(11)   NOT NULL,
  priority                   varchar(8)    NOT NULL,
  executor                   varchar(36),
  executor_appointed_by      varchar(36),
  deadline                   timestamp,
  last_updated_by            varchar(36)   NOT NULL,
  last_updated_date          timestamp     NOT NULL,
  CONSTRAINT pk_breakage_audit_id                    PRIMARY KEY (breakage, last_updated_date, last_updated_by),
  CONSTRAINT fk_breakage_audit_breakage_id           FOREIGN KEY (breakage)
        REFERENCES breakage (id),
  CONSTRAINT fk_breakage_audit_department            FOREIGN KEY (department)
        REFERENCES department (id),
  CONSTRAINT fk_breakage_audit_executor              FOREIGN KEY (executor)
        REFERENCES users (id),
  CONSTRAINT fk_breakage_audit_executor_appointed_by FOREIGN KEY (executor_appointed_by)
        REFERENCES users (id),
  CONSTRAINT fk_breakage_audit_last_updated_by       FOREIGN KEY (last_updated_by)
        REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS breakage_comment (
  id                varchar(36) NOT NULL,
  breakage          varchar(36) NOT NULL,
  comment           text        NOT NULL,
  created_by        varchar(36) NOT NULL,
  created_date      timestamp   NOT NULL,
  last_updated_by   varchar(36) NOT NULL,
  last_updated_date timestamp   NOT NULL,
  CONSTRAINT pk_breakage_comment_id              PRIMARY KEY (id),
  CONSTRAINT fk_breakage_comment_breakage_id     FOREIGN KEY (breakage)
        REFERENCES breakage (id),
  CONSTRAINT fk_breakage_comment_created_by      FOREIGN KEY (created_by)
        REFERENCES users (id),
  CONSTRAINT fk_breakage_comment_last_updated_by FOREIGN KEY (last_updated_by)
        REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS breakage_comment_audit (
  operation         char(1)     NOT NULL,
  breakage_comment  varchar(36) NOT NULL,
  breakage          varchar(36) NOT NULL,
  comment           text        NOT NULL,
  last_updated_by   varchar(36) NOT NULL,
  last_updated_date timestamp   NOT NULL,
  CONSTRAINT pk_breakage_comment_audit_id          PRIMARY KEY (breakage_comment, last_updated_date),
  CONSTRAINT fk_breakage_comment_audit_breakage_id FOREIGN KEY (breakage)
        REFERENCES breakage (id),
  CONSTRAINT fk_breakage_audit_last_updated_by     FOREIGN KEY (last_updated_by)
        REFERENCES users (id)
);


CREATE INDEX IF NOT EXISTS idx_breakage_comment_breakage ON breakage_comment(breakage);


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


CREATE OR REPLACE PROCEDURE set_current_id(
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
;

CREATE OR REPLACE FUNCTION delete_all_users_from_department() RETURNS trigger AS '
BEGIN
	UPDATE users
		SET enabled = false
		WHERE department = OLD.id;
	RETURN OLD;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit_breakage() RETURNS trigger AS '
BEGIN
    IF TG_OP = ''INSERT'' THEN
	    INSERT INTO breakage_audit
	        SELECT ''I'', nt.id, nt.department, nt.room, nt.breakage_topic, nt.breakage_text, nt.status, nt.priority,
	            nt.executor, nt.executor_appointed_by, nt.deadline, nt.last_updated_by, nt.last_updated_date
	        FROM new_table AS nt;
	ELSEIF TG_OP = ''UPDATE'' THEN
	    INSERT INTO breakage_audit
	        SELECT ''U'', nt.id, nt.department, nt.room, nt.breakage_topic, nt.breakage_text, nt.status, nt.priority,
        	    nt.executor, nt.executor_appointed_by, nt.deadline, nt.last_updated_by, nt.last_updated_date
        	FROM new_table AS nt;
    END IF;
	RETURN NULL;
END
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION audit_breakage_comment() RETURNS trigger AS '
BEGIN
    IF TG_OP = ''INSERT'' THEN
	    INSERT INTO breakage_comment_audit
	        SELECT ''I'', nt.id, nt.comment, nt.last_updated_by, nt.last_updated_date, nt.breakage
	        FROM new_table AS nt;
	ELSEIF TG_OP = ''UPDATE'' THEN
	    INSERT INTO breakage_comment_audit
	        SELECT ''U'', nt.id, nt.comment, nt.last_updated_by, nt.last_updated_date, nt.breakage
        	FROM new_table AS nt;
    ELSEIF TG_OP = ''DELETE'' THEN
    	INSERT INTO breakage_comment_audit
    	    SELECT ''D'', ot.id, ot.comment, ot.last_updated_by, NOW()::timestamp, ot.breakage
            FROM old_table AS ot;
    END IF;
	RETURN NULL;
END
' LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS delete_all_users_from_department ON department;

DROP TRIGGER IF EXISTS audit_breakage_create ON breakage;

DROP TRIGGER IF EXISTS audit_breakage_update ON breakage;

DROP TRIGGER IF EXISTS audit_breakage_comment_create ON breakage_comment;

DROP TRIGGER IF EXISTS audit_breakage_comment_update ON breakage_comment;

DROP TRIGGER IF EXISTS audit_breakage_comment_delete ON breakage_comment;


CREATE TRIGGER delete_all_users_from_department AFTER UPDATE OF enabled ON department
FOR EACH ROW EXECUTE PROCEDURE delete_all_users_from_department();

CREATE TRIGGER audit_breakage_create AFTER INSERT ON breakage
REFERENCING NEW TABLE AS new_table
FOR EACH STATEMENT EXECUTE PROCEDURE audit_breakage();

CREATE TRIGGER audit_breakage_update AFTER UPDATE ON breakage
REFERENCING NEW TABLE AS new_table
FOR EACH STATEMENT EXECUTE PROCEDURE audit_breakage();

CREATE TRIGGER audit_breakage_comment_create AFTER INSERT ON breakage_comment
REFERENCING NEW TABLE AS new_table
FOR EACH STATEMENT EXECUTE PROCEDURE audit_breakage_comment();

CREATE TRIGGER audit_breakage_comment_update AFTER UPDATE ON breakage_comment
REFERENCING NEW TABLE AS new_table
FOR EACH STATEMENT EXECUTE PROCEDURE audit_breakage_comment();

CREATE TRIGGER audit_breakage_comment_delete AFTER DELETE ON breakage_comment
REFERENCING OLD TABLE AS old_table
FOR EACH STATEMENT EXECUTE PROCEDURE audit_breakage_comment();

