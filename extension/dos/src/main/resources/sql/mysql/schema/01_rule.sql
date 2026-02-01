CREATE TABLE dos_rules
(
    id           integer                                                 not null auto_increment,
    natural_id   varchar(100)                                            not null,

    name         VARCHAR(200)                                            NOT NULL,
    address      VARCHAR(200)                                            NOT NULL,
    hostname     VARCHAR(200),
    tags         VARCHAR(200),

    active       BOOLEAN                                  DEFAULT TRUE   NOT NULL,

    type         ENUM ('IP','CIDR')                       DEFAULT 'CIDR' NOT NULL,
    action       ENUM ('DENY','ALLOW','THROTTLE', 'AUTO') DEFAULT 'AUTO' NOT NULL,

    request_rate VARCHAR(50),

    created_at   DATETIME                                                NOT NULL,
    modified_at  DATETIME                                                NOT NULL,

    description  VARCHAR(4000),

    CONSTRAINT pk$dos_rule$id PRIMARY KEY (id),
    constraint nk$dos_rule$natural_id unique key (natural_id)
) ENGINE INNODB;

CREATE TABLE dos_rule_stats
(
    id             integer      NOT NULL,
    name           VARCHAR(200) NOT NULL,

    country        VARCHAR(200),
    country_code   VARCHAR(200),
    region         VARCHAR(200),
    region_code    VARCHAR(200),
    city           VARCHAR(200),
    latitude       DOUBLE,
    longitude      DOUBLE,

    request_count  BIGINT       NOT NULL,
    deny_count     BIGINT       NOT NULL,
    throttle_count BIGINT       NOT NULL,

    created_at     DATETIME     NOT NULL,
    modified_at    DATETIME     NOT NULL,

    description    VARCHAR(4000),

    CONSTRAINT pk$dos_rule_stats$id PRIMARY KEY (id)
) ENGINE INNODB;

