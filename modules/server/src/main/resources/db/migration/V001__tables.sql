CREATE TABLE person
(
    id   BIGSERIAL    NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT person_pk PRIMARY KEY (id)
);

CREATE TABLE label
(
    id   BIGSERIAL    NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT label_pk PRIMARY KEY (id)
);

CREATE TABLE note
(
    id        BIGSERIAL    NOT NULL,
    title     VARCHAR(255) NOT NULL,
    message   TEXT         NOT NULL,
    status    VARCHAR(10)  NOT NULL,
    parent_id BIGINT,
    CONSTRAINT note_pk PRIMARY KEY (id),
    CONSTRAINT note_note_fk FOREIGN KEY (parent_id) REFERENCES note (id)
);

CREATE TABLE note_person
(
    note_id   BIGINT      NOT NULL,
    person_id BIGINT      NOT NULL,
    role      VARCHAR(10) NOT NULL,
    CONSTRAINT note_person_pk PRIMARY KEY (note_id, person_id, role),
    CONSTRAINT note_person_note_fk FOREIGN KEY (note_id) REFERENCES note (id),
    CONSTRAINT note_person_person_fk FOREIGN KEY (person_id) REFERENCES person (id)
);

CREATE TABLE note_label
(
    note_id  BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    CONSTRAINT note_label_pk PRIMARY KEY (note_id, label_id),
    CONSTRAINT note_label_note_fk FOREIGN KEY (note_id) REFERENCES note (id),
    CONSTRAINT note_label_label_fk FOREIGN KEY (label_id) REFERENCES label (id)
);
