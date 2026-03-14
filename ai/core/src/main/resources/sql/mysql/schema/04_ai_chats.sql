create table ai_chats
(
    id                  varchar(100) not null primary key,
    user_id             varchar(50)  not null,
    model_id            int          not null,
    prompt_id           int          not null,
    name                varchar(100) not null,

    start_at            datetime     not null,
    finish_at           datetime,
    duration            int          not null,

    token_count         int          not null,
    time_to_first_token int          not null,

    prompt_uri          varchar(500) not null,
    memory_uri          varchar(500) not null,
    tools_uri           varchar(500) not null,
    logs_uri            varchar(500) not null,

    tags                varchar(500),
    description         varchar(1000),
    constraint fk$ai_chats$user foreign key (user_id) references security_users (username),
    constraint fk$ai_chats$model foreign key (model_id) references ai_models (id),
    constraint fk$ai_chats$prompt foreign key (prompt_id) references ai_prompts (id)
) ENGINE = InnoDB;
