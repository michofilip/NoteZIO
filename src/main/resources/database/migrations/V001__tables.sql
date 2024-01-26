CREATE TABLE public.note
(
    id      BIGSERIAL   NOT NULL,
    title   VARCHAR(50) NOT NULL,
    message TEXT        NOT NULL,
    status  VARCHAR(10) NOT NULL,
    CONSTRAINT note_pk PRIMARY KEY (id)
);

CREATE TABLE person
(
    id   BIGSERIAL   NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE note_person
(
    note_id   BIGINT  NOT NULL,
    person_id BIGINT  NOT NULL,
    owner     BOOLEAN NOT NULL,
    PRIMARY KEY (note_id, person_id),
    FOREIGN KEY (note_id) REFERENCES note (id),
    FOREIGN KEY (person_id) REFERENCES person (id)
);
