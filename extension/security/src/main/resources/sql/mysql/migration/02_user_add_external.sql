alter table security_users add column external boolean default false not null;
alter table security_users add column provider_id varchar(100);
alter table security_users drop constraint fk$security_users$email;