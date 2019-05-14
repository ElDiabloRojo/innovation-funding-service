-- migrate verification tokens
UPDATE token t
INNER JOIN user_organisation uo ON uo.user_id=t.class_pk
SET t.extra_info=concat(trim(trailing'}' from t.extra_info), ', "organisationId":', uo.organisation_id, '}')
WHERE t.class_name='org.innovateuk.ifs.user.domain.User' AND t.extra_info IS NOT NULL;

DROP TABLE user_organisation;


-- process_role user_id, role_id, and application_id cannot be null
-- this needs no writes to the db
ALTER TABLE process_role
  DROP FOREIGN KEY FK_gm7bql0vdig803ktf5pc5mo2b,
  MODIFY user_id BIGINT(20) NOT NULL;
ALTER TABLE process_role ADD CONSTRAINT fk_process_role_user FOREIGN KEY (user_id) REFERENCES user(id);

ALTER TABLE process_role
  DROP FOREIGN KEY FK_j0syxe9gnfpvde1f6mqtul154,
  MODIFY role_id BIGINT(20) NOT NULL;
ALTER TABLE process_role ADD CONSTRAINT fk_process_role_role FOREIGN KEY (role_id) REFERENCES role(id);

ALTER TABLE process_role
  DROP FOREIGN KEY FK_gwtw85iv3vxq2914vxbluc8e9,
  MODIFY application_id BIGINT(20) NOT NULL;
ALTER TABLE process_role ADD CONSTRAINT fk_process_role_application FOREIGN KEY (application_id) REFERENCES application(id);


-- users can't have the same role more than once with respect to an application
-- prod should have been cleaned up so this is true
-- TODO integration test data contains duplicates
-- CREATE UNIQUE INDEX user_application_role_UNIQUE ON process_role(user_id, application_id, role_id);