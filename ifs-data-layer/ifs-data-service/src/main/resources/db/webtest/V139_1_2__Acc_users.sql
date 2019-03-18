SET @live_project_user =
(SELECT id FROM role WHERE name = 'live_projects_user');

SET @applicant_user =
(SELECT id FROM role WHERE name = 'applicant');

SET @max_before_id = (SELECT max(id) from user) - 1;

INSERT INTO `ifs`.`user`
(`email`, `first_name`, `last_name`, `status`, `uid`, `system_user`, `allow_marketing_emails`, `created_by`, `created_on`, `modified_by`, `modified_on`)
VALUES
('john.smith@euimicroresearch.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('rohn.rmith@euimicroresearch.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('MagdalenaMiller@monitoring.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('d.york@monitoring.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('adam.davies@euismallenthealth.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('maria.mcserman@health.care.clinin.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('prof.james.amerston@swindonuni.ac.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('blah@blah.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('james.black@euimeabs.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('j.white@argimachine.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('peter.potter@mrg.eu.trs.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('testman1@testing.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('testman2@testing.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('paul.knight@agrtd.fr.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('maria.costa@monitoring.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('r.parker@cit.ac.uk.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('ebrennan@limotors.org.uk.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('bl123@bl123.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('lgrsdfeen@tier.test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('aa4@bestmanufacturing.test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact1@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact11@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact1c@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact1d@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact20@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact21@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact22@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact23@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact3_0@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact31@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact32@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact33@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact30@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact4@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact5@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('contact29@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('r.vlagislav@highperfcomp.com.test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('jdieijf@jsadfi.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('rrr@highperfcomp.com.test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.smith@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('b.baron@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('c.red@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.jones@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.brown@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('b.potter@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('c.manton@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('b.steven@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('c.mcelek@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('greg.maddux@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.black@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('abc@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('def@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('xyz@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('tyr@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('james.blue@euimeabs.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('james.red@euimeabs.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('james.green@euimeabs.test', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('h@hthth.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('c@ccc2.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('amanda.g@businessmgr.test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('eee@businessmgr.test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('fff@businessmgr.test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('cfreeman@cloud9.test.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('john.barnes@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('bobby.moore@test.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('tyy1@tests.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('tyy2@tests.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('JohnSmith@farming.test.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('JamesSmithy@farming.test.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('JonathanSchmidt@farming.test.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('tt@farming.test.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('john.smith@testtesttest.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('retret@testtesttest.co.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('al.baker@imaginary.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('bleh2@bleh22.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('ames.cooper@imaginary.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('aroberts@learnuk.test.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('shiggins@teach.test.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('lbennett@educ4te.test.uk', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('rierie@rie.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('tietie@tie.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('jsmit@jksd.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('jsm333it@jksd.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('amiller@agm.test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('er@agm.test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('aoibheannmiller@agm.test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('erererer@agm.test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('apitest1@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('apitest2@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('apitest3@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('fc1@fc1.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('fc2@fc2.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('fc3@fc3.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('fc4@fc4.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('bluetest1@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('bluetest2@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('bluetest3@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('bluetest4@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('redtest1@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('redtest2@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('redtest3@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('greentest1@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('greentest2@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('greentest3@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('sebtest@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('szt1@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('szt2@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('szt3@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('wc@wc.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('ks@ks.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('tls@tls.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('aa@sdfsd.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('jjohnson@test840.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('sseller@test840.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('llennard@test840.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('qwerty1@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('qwerty2@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('qwerty3@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('qwerty4@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('asdf1@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('asdf2@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('asdf3@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('m.aranburu@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('m.oyarzabal@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('i.Zubeldia@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('j.Bautista@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('m.hernandez@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('l.Sangalli@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.Elustondo@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.Barrenetxea@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('d.llorente@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('g.rulli@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.illarramendi@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('r.pardo@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('s.ramirez@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('z.Zurutuza@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('m.Merino@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('j.zaldua@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.Gorosabel@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('m.Merquelanz@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('a.Guevara@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW()),
('n.Djouahra@test.com', 'John', 'Smith', 'ACTIVE', UUID(), '0', '0', '16', NOW(), '16', NOW());

SET @max_after_id = (SELECT max(id) from user);

DELIMITER //

CREATE PROCEDURE insert_user_roles()
BEGIN

  REPEAT

    SET @max_before_id = @max_before_id + 1;

    INSERT INTO user_role
        (user_id, role_id)
    VALUES
    (@max_before_id, @live_project_user);

    INSERT INTO user_role
        (user_id, role_id)
    VALUES
    (@max_before_id, @applicant_user);

  UNTIL @max_before_id = @max_after_id END REPEAT;

END //

CALL insert_user_roles() //

DROP PROCEDURE insert_user_roles //

