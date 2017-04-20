-- add new APPLICATION activity_type and NOT_APPLICABLE_INFORMED state enum values
ALTER TABLE activity_state MODIFY
  activity_type ENUM(
    'APPLICATION_ASSESSMENT','PROJECT_SETUP','PROJECT_SETUP_COMPANIES_HOUSE_DETAILS','PROJECT_SETUP_PROJECT_DETAILS',
    'PROJECT_SETUP_MONITORING_OFFICER_ASSIGNMENT','PROJECT_SETUP_BANK_DETAILS','PROJECT_SETUP_FINANCE_CHECKS',
    'PROJECT_SETUP_VIABILITY','PROJECT_SETUP_ELIGIBILITY','PROJECT_SETUP_SPEND_PROFILE',
    'PROJECT_SETUP_GRANT_OFFER_LETTER', 'APPLICATION') NOT NULL,
  MODIFY state ENUM('CREATED','PENDING','REJECTED','ACCEPTED','WITHDRAWN','OPEN','READY_TO_SUBMIT','SUBMITTED','VERIFIED',
    'NOT_VERIFIED','ASSIGNED','NOT_ASSIGNED','NOT_APPLICABLE','NOT_APPLICABLE_INFORMED') NOT NULL;

-- add the APPLICATION activity_states
INSERT INTO activity_state (activity_type, state) VALUES
  ('APPLICATION', 'CREATED'),
  ('APPLICATION', 'OPEN'),
  ('APPLICATION', 'SUBMITTED'),
  ('APPLICATION', 'NOT_APPLICABLE'),
  ('APPLICATION', 'NOT_APPLICABLE_INFORMED'),
  ('APPLICATION', 'ACCEPTED'),
  ('APPLICATION', 'REJECTED');

-- add a backing process to each existing application
INSERT INTO process (process_type, last_modified, target_id, activity_state_id)
SELECT 'ApplicationProcess', now(), id, (SELECT id FROM activity_state WHERE activity_type='APPLICATION' AND state = IF (status = 'APPROVED', 'ACCEPTED', status)) FROM application;

-- drop the  status column from application
ALTER TABLE application DROP COLUMN status;