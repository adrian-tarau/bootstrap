create table security_users
(
    username    varchar(50)          not null primary key,
    name        varchar(100)         not null,
    password    varchar(500)         not null,
    token       varchar(500),
    email       varchar(200),
    `enabled`   boolean default true not null,
    reset_password boolean default false not null,
    external    boolean default false not null,
    provider_id varchar(100),
    created_at  datetime             not null,
    modified_at datetime,
    description varchar(1000)
) ENGINE = InnoDB;

create table security_authorities
(
    username   varchar(50) not null,
    authority  varchar(50) not null,
    created_at datetime    default CURRENT_TIMESTAMP not null,
    constraint pk$security_authorities primary key (username, authority),
    constraint fk$security_authorities$user foreign key (username) references security_users (username)
) ENGINE = InnoDB;

create unique index ix$security_authorities$username on security_authorities (username, authority);

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