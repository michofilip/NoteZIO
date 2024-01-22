CREATE TABLE public.note
(
    id      BIGSERIAL   NOT NULL,
    title   VARCHAR(50) NOT NULL,
    message TEXT        NOT NULL,
    status  VARCHAR(10) NOT NULL,
    CONSTRAINT note_pk PRIMARY KEY (id)
);

CREATE TABLE "user"
(
    id   BIGSERIAL   NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE note_user
(
    note_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (note_id, user_id),
    FOREIGN KEY (note_id) REFERENCES note (id),
    FOREIGN KEY (user_id) REFERENCES "user" (id)
);