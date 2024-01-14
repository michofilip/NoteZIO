CREATE TABLE public.note
(
    id      serial8     NOT NULL,
    title   varchar(50) NOT NULL,
    message text        NOT NULL,
    status  varchar(10) NOT NULL,
    CONSTRAINT note_pk PRIMARY KEY (id)
);

CREATE TABLE public.user
(
    id   serial8     NOT NULL,
    name varchar(50) NOT NULL,
    CONSTRAINT user_pk PRIMARY KEY (id)
);

CREATE TABLE public.note_user
(
    note_id bigint NOT NULL,
    user_id bigint NOT NULL,
    CONSTRAINT note_user_note_fk FOREIGN KEY (note_id) REFERENCES public.note (id),
    CONSTRAINT note_user_user_fk FOREIGN KEY (user_id) REFERENCES public.user (id)
);