create table rest_api_client_audits
(
    id            integer                                        not null,
    client_id     integer                                        not null,
    name          varchar(100)                                   not null,
    http_method   ENUM ('GET', 'POST', 'PUT', 'DELETE', 'PATCH') not null,
    http_status   integer                                        not null,
    success       boolean                                        not null,
    started_at    datetime                                       not null,
    ended_at      datetime                                       not null,
    duration      int                                            not null,
    request_path  varchar(1000)                                  not null,
    query_params  varchar(1000),

    error_message varchar(1000),

    constraint pk$rest_api_client_requests primary key (id),
    constraint fk$rest_api_clients$client_id foreign key (client_id) references rest_api_clients (id)
) ENGINE = InnoDB;

create index ix$rest_api_client_request$started on rest_api_client_audits (started_at);