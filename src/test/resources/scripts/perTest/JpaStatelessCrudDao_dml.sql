DELETE FROM x;
DELETE FROM q;
DELETE FROM suby;
DELETE FROM y;
DELETE FROM e;
DELETE FROM openjpa_sequence_table;

INSERT INTO e (persistenceId, name, persistenceversion) VALUES (1, 'test', 1);

INSERT INTO y (persistenceId, persistenceversion) VALUES (1, 1);
INSERT INTO y (persistenceId, persistenceversion) VALUES (2, 1);
INSERT INTO y (persistenceId, persistenceversion) VALUES (3, 1);

INSERT INTO suby (persistenceId , active) VALUES (1, 0);
INSERT INTO suby (persistenceId , active) VALUES (2, 1);
INSERT INTO suby (persistenceId , active) VALUES (3, 1);

INSERT INTO q (ID, name, description, locale, suby_fk) VALUES (1, 'name 1', 'description 1', 'nl__', 1);
INSERT INTO q (ID, name, description, locale, suby_fk) VALUES (2, 'name 2', 'description 2', 'en__', 1);
INSERT INTO q (ID, name, description, locale, suby_fk) VALUES (3, 'name 3', 'description 3', 'fr__', 1);
INSERT INTO q (ID, name, description, locale, suby_fk) VALUES (4, 'name 4', 'description 4', 'de__', 2);
INSERT INTO q (ID, name, description, locale, suby_fk) VALUES (5, 'name 5', 'description 5', 'ca__', 2);
INSERT INTO q (ID, name, description, locale, suby_fk) VALUES (6, 'name 6', 'description 6', 'ca__', 3);

INSERT INTO x (persistenceid, description, locked, period, y_fk,persistenceversion, e_fk)
  VALUES (1, 'test x', 0, {d '2008-10-16'}, 1, 0, 1);

INSERT INTO x (persistenceid, description, locked, period, y_fk,persistenceversion, e_fk)
  VALUES (2, 'test x 2', 0, {d '2008-10-16'}, 2, 0, 1);

INSERT INTO x (persistenceid, description, locked, period, y_fk,persistenceversion, e_fk)
  VALUES (3, 'test x 3', 0, {d '2008-10-16'}, 3, 0, 1);

INSERT INTO openjpa_sequence_table (id, sequence_value) VALUES (0, 1000);
