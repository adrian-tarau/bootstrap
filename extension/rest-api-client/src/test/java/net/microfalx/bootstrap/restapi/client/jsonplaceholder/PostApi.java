package net.microfalx.bootstrap.restapi.client.jsonplaceholder;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;

public interface PostApi {

    @GET("/posts")
    Call<Collection<Post>> list();

    @GET("/posts/{id}")
    Call<Post> get(@Path("id") int id);

    @POST("/posts")
    Call<Post> create(@Body Post post);

    @PUT("/posts/{id}")
    Call<Post> update(@Path("id") int id, @Body Post post);

    @DELETE("/posts/{id}")
    Call<Void> delete(@Path("id") int id);
}
