alter table ai_chats rename column resource TO memory_uri;
alter table ai_chats add column prompt_uri varchar(500) not null;
alter table ai_chats add column logs_uri varchar(500) not null;
alter table ai_chats add column tools_uri varchar(500) not null;
alter table ai_chats add column time_to_first_token int not null;