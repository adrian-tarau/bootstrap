INSERT INTO registry_nodes (parent_id, natural_id, path, update_count, version, created_at, modified_at)
    VALUES (?, ?, ?, ?, ?, ?, ?)
on duplicate key update update_count = values(update_count) + 1, version = values(version), modified_at = values(modified_at)