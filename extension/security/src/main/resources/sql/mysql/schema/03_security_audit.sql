create table security_audit
(
    id          integer       not null auto_increment primary key,
    username    varchar(50)   not null,
    `action`    varchar(100)  not null,
    module      varchar(100)  not null,
    category    varchar(100)  not null,
    client_info varchar(100)  not null,
    reference   varchar(1000) not null,
    error_code  varchar(100)  not null,
    created_at  datetime      not null,
    description varchar(1000),
    constraint fk$security_audit$user foreign key (username) references security_users (username)
) ENGINE = InnoDB;