CREATE TABLE job_architecture_item_skills
(
    item_id     UUID         NOT NULL,
    skill_id    UUID         NOT NULL,
    skill_level SMALLINT     NOT NULL DEFAULT 0,

    CONSTRAINT job_architecture_item_skills_pk
        PRIMARY KEY (item_id, skill_id),
    CONSTRAINT job_architecture_item_skills_item_fk
        FOREIGN KEY (item_id)
            REFERENCES job_architecture_items (id)
            ON DELETE CASCADE
);
