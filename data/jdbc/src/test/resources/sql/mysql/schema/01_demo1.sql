create table demo1
(
    demo1_key   varchar(50)  not null primary key,
    name        varchar(100) not null,
    created_at  datetime     not null,
    modified_at datetime,
    description varchar(1000),
    constraint fk$security_users$email unique key (name)
) ENGINE = InnoDB;

create table demo1_custom
(
    demo1_key   varchar(50)  not null,
    name        varchar(200) not null,
    value       mediumblob   null,
    created_at  datetime     not null,
    modified_at datetime,
    constraint pk$security_users_settings primary key (demo1_key, name),
    constraint fk$security_users_settings$user foreign key (demo1_key) references demo1 (demo1_key)
) ENGINE = InnoDB;