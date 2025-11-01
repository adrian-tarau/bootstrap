create table ai_chats
(
    id          varchar(100) not null primary key,
    user_id     varchar(50)  not null,
    model_id    int          not null,
    name        varchar(100) not null,
    start_at    datetime     not null,
    finish_at   datetime,
    resource    varchar(500) not null,
    tags        varchar(500),
    token_count int          not null,
    duration    int          not null,
    description varchar(1000),
    constraint fk$ai_chats$user_id foreign key (user_id) references security_users (username),
    constraint fk$ai_chats$model_id foreign key (model_id) references ai_models (id)
) ENGINE = InnoDB;
