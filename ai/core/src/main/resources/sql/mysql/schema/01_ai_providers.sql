create table ai_providers
(
    id          int          not null primary key auto_increment,
    natural_id  varchar(100) not null,
    name        varchar(100) not null,
    uri         varchar(1000),
    api_key     varchar(500),
    author      varchar(100)  default '',
    license     varchar(1000) default 'Proprietary',
    version     varchar(50)   default '',
    created_at  datetime     not null,
    modified_at datetime,
    tags        varchar(500),
    description varchar(1000),
    constraint nk$ai_providers$natural_id unique key (natural_id)
) ENGINE = InnoDB;