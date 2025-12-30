package net.microfalx.bootstrap.restapi.client;

import net.microfalx.bootstrap.restapi.client.exception.ApiError;
import net.microfalx.bootstrap.restapi.client.exception.ApiException;
import net.microfalx.bootstrap.restapi.client.exception.ServerErrorException;
import net.microfalx.lang.ClassUtils;
import retrofit2.Call;
import retrofit2.Response;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A wrapper around a REST API call.
 *
 * @param <A> the API type
 * @param <R> the response type
 */
class RestApiCallWrapper<A, R> implements InvocationHandler {

    private final RestApiWrapper<A> apiWrapper;
    private final Call<R> call;
    private final Method method;

    RestApiCallWrapper(RestApiWrapper<A> apiWrapper, Call<R> call, Method method) {
        requireNonNull(apiWrapper);
        requireNonNull(call);
        requireNonNull(method);
        this.apiWrapper = apiWrapper;
        this.call = call;
        this.method = method;
    }

    RestClient getClient() {
        return apiWrapper.getClient();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String name = method.getName();
        RestClient restClient = getClient();
        if ("toString".equals(name)) {
            return "REST API Call for " + ClassUtils.getName(apiWrapper.getApiType()) + "." + this.method.getName() + " [" + restClient.getUri() + "]";
        } else {
            restClient.attach();
            try {
                return doInvoke(method, args);
            } finally {
                restClient.detach();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object doInvoke(Method method, Object[] args) throws Throwable {
        final String name = method.getName();
        try {
            if ("execute".equals(name)) {
                Response<Object> response = (Response<Object>) method.invoke(call, args);
                if (!response.isSuccessful()) throw getClient().map(response);
                return response;
            } else {
                return method.invoke(call, args);
            }
        } catch (ApiException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServerErrorException(500, new ApiError().setStatus(500).setMessage(e.getMessage()), e);
        }

    }
}
