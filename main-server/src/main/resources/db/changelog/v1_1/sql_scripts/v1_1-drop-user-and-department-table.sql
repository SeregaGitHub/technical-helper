DROP TRIGGER IF EXISTS delete_all_users_from_department ON department

GO

DROP FUNCTION IF EXISTS delete_all_users_from_department()

GO

DROP PROCEDURE set_current_id(
                              IN dep_id varchar, IN adm_id varchar,
                              IN dep_created_by varchar, IN adm_created_by varchar,
                              IN dep_last_updated_by varchar, IN adm_last_updated_by varchar,
                              IN temporary_admin_id varchar
                             )

GO

DROP PROCEDURE drop_constraints()

GO

ALTER TABLE users
	DROP CONSTRAINT fk_user_last_updated_by

GO

ALTER TABLE users
	DROP CONSTRAINT fk_user_created_by

GO

ALTER TABLE department
	DROP CONSTRAINT fk_department_last_updated_by

GO

ALTER TABLE department
	DROP CONSTRAINT fk_department_created_by

GO

ALTER TABLE users
	DROP CONSTRAINT fk_users_department

GO

DROP TABLE users

GO

DROP TABLE department

GO

DROP TYPE user_role

GO
