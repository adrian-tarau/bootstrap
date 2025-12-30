create table rest_api_clients
(
    id          integer      not null auto_increment,
    natural_id  varchar(50)  not null,
    name        varchar(100) not null,
    uri         varchar(500),
    api_key varchar(1000),
    created_at  datetime     not null,
    modified_at datetime,
    description varchar(1000),
    constraint pk$rest_api_clients primary key (id),
    constraint nk$rest_api_clients$natural_id unique key (natural_id)
) ENGINE = InnoDB;