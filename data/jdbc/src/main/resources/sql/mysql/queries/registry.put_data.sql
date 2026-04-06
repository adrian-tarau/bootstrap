INSERT INTO registry_data (id, data)
    VALUES (?, ?)
on duplicate key update data = values(data);