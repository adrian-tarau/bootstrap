create table if not exists database_migrations
(
    id             varchar(100) primary key,
    name           varchar(200)                           not null,
    module         varchar(200)                           not null,
    path           varchar(500)                           not null,
    applied_at     datetime                               not null,
    execution_time int                                    not null,
    status         enum ('SUCCESSFUL','FAILED','APPLIED') not null,
    checksum       varchar(100)                           not null,
    log            text
);