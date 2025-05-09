CREATE TABLE job_architecture_levels
(
    id     VARCHAR(8)   NOT NULL,
    parent VARCHAR(8),
    title  VARCHAR(128) NOT NULL,
    CONSTRAINT job_architecture_levels_pk
        PRIMARY KEY (id),
    CONSTRAINT job_architecture_levels_u
        UNIQUE (id, parent),
    CONSTRAINT job_architecture_levels_parent_fk
        FOREIGN KEY (parent)
            REFERENCES job_architecture_levels (id)
);

INSERT INTO job_architecture_levels(id, parent, title)
VALUES ('ROOT', NULL, 'Job Architecture root node'),
       ('FAMILY', 'ROOT', 'Job Families'),
       ('CLUSTER', 'FAMILY', 'Job Cluster'),
       ('ROLE', 'CLUSTER', 'Job Roles'),
       ('LEVEL', 'ROLE', 'Job Level');

CREATE TABLE job_architecture_items
(
    id             UUID         NOT NULL,
    level          VARCHAR(8)   NOT NULL,
    title          VARCHAR(256) NOT NULL,
    description    TEXT,
    parent         UUID,
    parent_level   VARCHAR(8),
    creator        UUID,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT job_architecture_items_pk
        PRIMARY KEY (id),
    CONSTRAINT job_architecture_items_u
        UNIQUE (id, level),
    CONSTRAINT job_architecture_items_parent_fk
        FOREIGN KEY (parent, parent_level)
            REFERENCES job_architecture_items (id, level)
            ON DELETE CASCADE,
    CONSTRAINT job_architecture_items_level_fk
        FOREIGN KEY (level, parent_level)
            REFERENCES job_architecture_levels (id, parent)
);
