create table security_groups
(
    id          integer              not null auto_increment primary key,
    name        varchar(100)         not null,
    `enabled`   boolean default true not null,
    created_at  datetime             not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table security_authorities
(
    username   varchar(50) not null,
    authority  varchar(50) not null,
    created_at datetime    default CURRENT_TIMESTAMP not null,
    constraint fk$security_authorities$user foreign key (username) references security_users (username)
) ENGINE = InnoDB;

create unique index ix$security_authorities$username on security_authorities (username, authority);

create table security_group_authorities
(
    group_id   integer     not null,
    authority  varchar(50) not null,
    created_at datetime    default CURRENT_TIMESTAMP not null,
    constraint pk$security_group_authorities primary key (group_id, authority),
    constraint fk$security_group_authorities$group foreign key (group_id) references security_groups (id)
) ENGINE = InnoDB;

create table security_group_members
(
    username   varchar(50) not null,
    group_id   integer     not null,
    created_at datetime    default CURRENT_TIMESTAMP not null,
    constraint pk$security_group_members primary key (group_id, username),
    constraint fk$security_group_members$group foreign key (group_id) references security_groups (id)
) ENGINE = InnoDB;