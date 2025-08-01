create table ai_provider
(
    id                              int                 not null primary key auto_increment,
    natural_id                      varchar(100)        not null,
    name                            varchar(100)        not null,
    uri                             varchar(1000),
    api_key                         varchar(500),
    author                          varchar(100)        default '',
    license                         varchar(1000)       default 'Proprietary',
    version                         varchar(50)         default '',
    created_at                      datetime            not null,
    modified_at                     datetime,
    tags                            varchar(500),
    description                     varchar(1000),
    constraint nk$ai_provider$natural_id unique key (natural_id)
) ENGINE = InnoDB;

create table ai_model
(
    id                              int                not null primary key auto_increment,
    natural_id                      varchar(100)       not null,
    provider_id                     int                not null,
    name                            varchar(100)       not null,
    uri                             varchar(1000),
    api_key                         varchar(500),
    enabled boolean default true not null,
    `default` boolean default false not null,
    embedding boolean not null,
    model_name                      varchar(100),
    temperature                     decimal,
    top_p                           decimal,
    top_k                           int,
    frequency_penalty               decimal,
    presence_penalty                decimal,
    thinking                        boolean default false not null,
    maximum_context_length          int                 not null,
    maximum_output_tokens           int,
    stop_sequences                  varchar(5000)       not null,
    response_format                 enum('TEXT','JSON') default 'TEXT',
    created_at                      datetime            not null,
    modified_at                     datetime,
    tags                            varchar(500),
    description                     varchar(1000),
    constraint nk$ai_model$natural_id unique key (natural_id),
    constraint fk$ai_model$provider_id foreign key (provider_id) references ai_provider (id)
) ENGINE = InnoDB;

create table ai_chat
(
    id                              varchar(100)        not null primary key,
    user_id                         varchar(50)         not null,
    model_id                        int                 not null,
    name                            varchar(100)        not null,
    start_at                        datetime            not null,
    finish_at                       datetime,
    resource                        varchar(500)        not null,
    tags                            varchar(500),
    token_count                     int                 not null,
    duration                        int                 not null,
    description                     varchar(1000),
    constraint fk$ai_chat$user_id foreign key (user_id) references security_users (username),
    constraint fk$ai_chat$model_id foreign key (model_id) references ai_model (id)
) ENGINE = InnoDB;

create table ai_prompt(
    id                              int                 not null primary key auto_increment,
    natural_id                      varchar(100)        not null,
    model_id                        int,
    name                            varchar(100)        not null,
    role                            mediumtext,
    maximum_input_events            int,
    maximum_output_tokens           int,
    chain_of_thought                boolean default false not null,
    use_only_context                boolean default true  not null,
    instructions                    mediumtext,
    examples                        mediumtext,
    context                         mediumtext,
    question                        mediumtext,
    thinking                        boolean default false not null,
    `system`                        boolean default false not null,
    created_at                      datetime            not null,
    modified_at                     datetime,
    tags                            varchar(500),
    description                     varchar(1000),
    constraint nk$ai_prompt$natural_id unique key (natural_id),
    constraint fk$ai_prompt$model_id foreign key (model_id) references ai_model (id)
) ENGINE = InnoDB;