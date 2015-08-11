/*
-- Query: SELECT concat('DROP TABLE IF EXISTS ', table_name, ';')
FROM information_schema.tables
WHERE table_schema = 'ifs-data'
-- Date: 2015-08-11 14:24
*/
SET foreign_key_checks = 0;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS application_finance;
DROP TABLE IF EXISTS application_status;
DROP TABLE IF EXISTS capital_usage;
DROP TABLE IF EXISTS competition;
DROP TABLE IF EXISTS labour;
DROP TABLE IF EXISTS materials;
DROP TABLE IF EXISTS organisation;
DROP TABLE IF EXISTS other_cost;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS question_type;
DROP TABLE IF EXISTS response;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS section;
DROP TABLE IF EXISTS sub_contractor;
DROP TABLE IF EXISTS travel_cost;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS user_application_role;
