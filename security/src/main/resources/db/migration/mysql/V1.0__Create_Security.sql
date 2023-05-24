create table users
(
    username varchar(50)  not null primary key,
    password varchar(500) not null,
    enabled  boolean      not null
);

create table authorities
(
    username  varchar(50) not null,
    authority varchar(50) not null,
    constraint fk_authorities_users foreign key (username) references users (username)
);
create unique index ix_auth_username on authorities (username, authority);

create table groups
(
    id         integer     not null auto_increment primary key,
    group_name varchar(50) not null
);

create table group_authorities
(
    group_id  integer     not null,
    authority varchar(50) not null,
    constraint fk_group_authorities_group foreign key (group_id) references groups (id)
);

create table group_members
(
    id       integer     not null auto_increment primary key,
    username varchar(50) not null,
    group_id integer     not null,
    constraint fk_group_members_group foreign key (group_id) references groups (id)
);