CREATE TABLE dos_audit
(
    id          integer                                                  not null auto_increment,
    rule_id     integer                                                  NOT NULL,

    uri         VARCHAR(1000)                                            NOT NULL,
    reason      ENUM ('DISCOVERY','DOS','SECURITY','SCAN') DEFAULT 'DOS' NOT NULL,

    created_at  DATETIME                                                 NOT NULL,
    description VARCHAR(4000),

    CONSTRAINT pk$dos_audit$id PRIMARY KEY (id),
    CONSTRAINT fk$dos_audit$rule FOREIGN KEY (rule_id) REFERENCES dos_rules (id)
) ENGINE INNODB;

create index ix$dos_audit$created on dos_audit (created_at);
create index ix$dos_audit$reason on dos_audit (reason);