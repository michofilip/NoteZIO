INSERT INTO public.note(title, message, status)
VALUES ('Note 1', 'Message 1', 'Draft'),
       ('Note 2', 'Message 2', 'Ongoing'),
       ('Note 3', 'Message 3', 'Ongoing'),
       ('Note 4', 'Message 4', 'Complete');

INSERT INTO public.user(name)
VALUES ('Ala'),
       ('Ela'),
       ('Ola'),
       ('Ula');

INSERT INTO public.note_user(note_id, user_id)
VALUES (1, 1),
       (1, 1),
       (2, 2),
       (3, 3),
       (4, 4);
