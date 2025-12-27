package net.microfalx.bootstrap.restapi.client;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class RestApiCallAdapter<R> implements CallAdapter<R, R> {

    private final RestClient client;
    private final Type returnType;

    RestApiCallAdapter(RestClient client, Type returnType) {
        this.client = client;
        this.returnType = returnType;
    }

    @Override
    public Type responseType() {
        return returnType;
    }

    @Override
    public R adapt(Call<R> call) {
        return client.execute(call);
    }

    protected static class Factory extends CallAdapter.Factory {

        private final RestClient client;

        public Factory(RestClient client) {
            this.client = client;
        }

        @Override
        public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
            if (returnType instanceof ParameterizedType parameterizedType) {
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class<?> classType) {
                    if (classType.equals(Call.class)) {
                        return null;
                    }
                }
            }
            return new RestApiCallAdapter<>(client, returnType);
        }
    }
}
