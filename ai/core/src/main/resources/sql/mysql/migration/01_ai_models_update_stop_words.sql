alter table ai_models modify column stop_sequences varchar(5000);
alter table ai_models modify column response_format enum ('TEXT','JSON') default 'TEXT' not null;