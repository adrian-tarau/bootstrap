insert into database_migrations (id, name, module, path, applied_at, execution_time, status, checksum, log)
values (?, ?, ?, ?, ?, ?, ?, ?, ?)
on duplicate KEY UPDATE status         = VALUES(status),
                        log            = VALUES(log),
                        execution_time = VALUES(execution_time);