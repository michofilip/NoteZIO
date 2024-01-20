CREATE TABLE public.note
(
    id      BIGSERIAL   NOT NULL,
    title   VARCHAR(50) NOT NULL,
    message TEXT        NOT NULL,
    status  VARCHAR(10) NOT NULL,
    CONSTRAINT note_pk PRIMARY KEY (id)
);

CREATE TABLE public.user
(
    id   BIGSERIAL   NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT user_pk PRIMARY KEY (id)
);

CREATE TABLE public.note_user
(
    note_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (note_id, user_id),
    CONSTRAINT note_user_note_fk FOREIGN KEY (note_id) REFERENCES public.note (id),
    CONSTRAINT note_user_user_fk FOREIGN KEY (user_id) REFERENCES public.user (id)
);