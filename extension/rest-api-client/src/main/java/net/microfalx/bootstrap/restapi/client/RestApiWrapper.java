package net.microfalx.bootstrap.restapi.client;

import lombok.AccessLevel;
import lombok.Getter;
import net.microfalx.lang.ClassUtils;
import retrofit2.Call;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A wrapper on top of Retrofit API interfaces.
 * <p>
 * The wrapper will handle additional concerns like security, better logging, etc.
 */
@Getter(AccessLevel.PACKAGE)
class RestApiWrapper<A> implements InvocationHandler {

    private final RestClient client;
    private final Class<A> apiType;
    private final A api;

    RestApiWrapper(RestClient client, Class<A> apiType, A api) {
        requireNonNull(client);
        requireNonNull(apiType);
        requireNonNull(api);
        this.client = client;
        this.apiType = apiType;
        this.api = api;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String name = method.getName();
        if ("toString".equals(name)) {
            return "REST API for " + ClassUtils.getName(apiType) + " [" + client.getUri() + "]";
        } else {
            RestClient.CLIENT.set(client);
            try {
                Object result = method.invoke(api, args);
                if (result instanceof Call<?>) {
                    return wrapCall(method, result);
                } else {
                    return result;
                }
            } finally {
                RestClient.CLIENT.remove();
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object wrapCall(Method method, Object result) {
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Call.class}, new RestApiCallWrapper(this, (Call) result, method));
    }
}
