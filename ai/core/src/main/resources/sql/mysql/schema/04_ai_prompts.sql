create table ai_prompts
(
    id                    int                   not null primary key auto_increment,
    natural_id            varchar(100)          not null,
    model_id              int,
    name                  varchar(100)          not null,
    role                  mediumtext,
    maximum_input_events  int,
    maximum_output_tokens int,
    chain_of_thought      boolean default false not null,
    use_only_context      boolean default true  not null,
    instructions          mediumtext,
    examples              mediumtext,
    context               mediumtext,
    question              mediumtext,
    thinking              boolean default false not null,
    `system`              boolean default false not null,
    created_at            datetime              not null,
    modified_at           datetime,
    tags                  varchar(500),
    description           varchar(1000),
    constraint nk$ai_prompts$natural_id unique key (natural_id),
    constraint fk$ai_prompts$model_id foreign key (model_id) references ai_models (id)
) ENGINE = InnoDB;