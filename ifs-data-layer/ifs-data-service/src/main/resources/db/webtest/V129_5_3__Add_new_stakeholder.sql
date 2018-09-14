-- IFS-2994: Add new stakeholder.

SET @ifs_web_system_user_id =
(SELECT id FROM user WHERE email = 'ifs_web_user@innovateuk.org');

SET @stakeholder_role_id =
(SELECT id FROM role WHERE name = 'stakeholder');

INSERT INTO `user`
(email, first_name, last_name, phone_number, status, uid, system_user, allow_marketing_emails, created_by, created_on, modified_by, modified_on)
VALUES ('Rayon.Kevin@gmail.com', 'Rayon', 'Kevin', '449823463589', 'ACTIVE', 'a23d49e5-816e-4acf-b95a-5d712562e037', 0, 0, @ifs_web_system_user_id, NOW(), @ifs_web_system_user_id, NOW());

SET @new_stakeholder_user_id =
(SELECT id FROM user WHERE email = 'Rayon.Kevin@gmail.com');

INSERT INTO user_role
(user_id, role_id)
VALUES
(@new_stakeholder_user_id, @stakeholder_role_id);

insert into competition_user
(competition_id, competition_role, user_id, participant_status_id)
values
(23, 'STAKEHOLDER', @new_stakeholder_user_id, 2),
(24, 'STAKEHOLDER', @new_stakeholder_user_id, 2),
(11, 'STAKEHOLDER', @new_stakeholder_user_id, 2),
(35, 'STAKEHOLDER', @new_stakeholder_user_id, 2),
(16, 'STAKEHOLDER', @new_stakeholder_user_id, 2),
(10, 'STAKEHOLDER', @new_stakeholder_user_id, 2),
(18, 'STAKEHOLDER', @new_stakeholder_user_id, 2);


