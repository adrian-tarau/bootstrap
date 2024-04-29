package net.microfalx.bootstrap.metrics;

import net.microfalx.lang.annotation.Provider;

@Provider
public class TestRepository extends AbstractRepository {

    @Override
    public boolean supports(Query query) {
        return "test".equalsIgnoreCase(query.getType());
    }

    @Override
    public Result query(Query query) {
        if ("invalid".equalsIgnoreCase(query.getText())) {
            throw new QueryException("Invalid");
        } else {
            return Result.scalar(query, Value.zero());
        }
    }
}
