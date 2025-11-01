create table security_users
(
    username    varchar(50)          not null primary key,
    name        varchar(100)         not null,
    password    varchar(500)         not null,
    token       varchar(500),
    `enabled`   boolean default true not null,
    email       varchar(200),
    created_at  datetime             not null,
    modified_at datetime,
    description varchar(1000),
    constraint fk$security_users$email unique key (email)
) ENGINE = InnoDB;

create table security_users_settings
(
    username    varchar(50)  not null,
    name        varchar(200) not null,
    value       mediumblob   null,
    created_at  datetime     not null,
    modified_at datetime,
    constraint pk$security_users_settings primary key (username, name),
    constraint fk$security_users_settings$user foreign key (username) references security_users (username)
) ENGINE = InnoDB;