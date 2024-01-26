INSERT INTO note(title, message, status)
VALUES ('Note 1', 'Message 1', 'Draft'),
       ('Note 2', 'Message 2', 'Ongoing'),
       ('Note 3', 'Message 3', 'Ongoing'),
       ('Note 4', 'Message 4', 'Complete');

INSERT INTO person(name)
VALUES ('Ala'),
       ('Ela'),
       ('Ola'),
       ('Ula');

INSERT INTO note_person(note_id, person_id, owner)
VALUES (1, 1, TRUE),
       (1, 2, FALSE),
       (2, 2, TRUE),
       (3, 3, TRUE),
       (4, 4, TRUE);
