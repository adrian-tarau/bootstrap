create table registry_nodes
(
    id           bigint            not null auto_increment primary key,
    parent_id    bigint,
    natural_id   varchar(100)      not null,
    path         varchar(1000)     not null,
    update_count integer default 0 not null,
    version      integer default 0 not null,
    created_at   datetime          not null,
    modified_at  datetime          not null,
    constraint nk$registry_nodes$natural unique key (natural_id),
    constraint fk$registry_nodes$parent foreign key (parent_id) references registry_nodes (id)
) ENGINE = InnoDB;