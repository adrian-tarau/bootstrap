create table users
(
    username    varchar(50)          not null primary key,
    name        varchar(100)         not null,
    password    varchar(500)         not null,
    `enabled`   boolean default true not null,
    email       varchar(100)         not null,
    created_at  datetime             not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table authorities
(
    username   varchar(50) not null,
    authority  varchar(50) not null,
    created_at datetime    not null,
    constraint fk$authorities$user foreign key (username) references users (username)
) ENGINE = InnoDB;

create unique index ix$authorities$username on authorities (username, authority);

create table `groups`
(
    id          integer              not null auto_increment primary key,
    name        varchar(100)         not null,
    `enabled`   boolean default true not null,
    created_at  datetime             not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table group_authorities
(
    group_id   integer     not null,
    authority  varchar(50) not null,
    created_at datetime    not null,
    constraint fk$group_authorities$group foreign key (group_id) references `groups` (id)
) ENGINE = InnoDB;

create table group_members
(
    id         integer     not null auto_increment primary key,
    username   varchar(50) not null,
    group_id   integer     not null,
    created_at datetime    not null,
    constraint fk$group_members$group foreign key (group_id) references `groups` (id)
) ENGINE = InnoDB;