INSERT INTO person(name)
VALUES ('Ala'),
       ('Ela'),
       ('Ola'),
       ('Ula');

INSERT INTO label(name)
VALUES ('Label 1'),
       ('Label 2'),
       ('Label 3'),
       ('Label 4');

INSERT INTO note(title, message, status, parent_id)
VALUES ('Note 1', 'Message 1', 'Ongoing', null),
       ('Note 2', 'Message 2', 'Ongoing', 1),
       ('Note 3', 'Message 3', 'Draft', 2),
       ('Note 4', 'Message 4', 'Complete', 2),
       ('Note 5', 'Message 5', 'Draft', null);


INSERT INTO note_person(note_id, person_id, owner)
VALUES (1, 1, TRUE),
       (1, 2, FALSE),
       (2, 2, TRUE),
       (3, 3, TRUE);

INSERT INTO note_label(note_id, label_id)
VALUES (1, 1),
       (2, 1),
       (2, 2),
       (3, 3);
