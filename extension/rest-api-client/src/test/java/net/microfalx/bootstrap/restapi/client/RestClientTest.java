package net.microfalx.bootstrap.restapi.client;

import net.microfalx.bootstrap.restapi.client.exception.NotFoundException;
import net.microfalx.bootstrap.restapi.client.jsonplaceholder.Post;
import net.microfalx.bootstrap.restapi.client.jsonplaceholder.PostApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class RestClientTest {

    @InjectMocks
    private RestClientService restClientService;
    private RestClient restClient;

    @BeforeEach
    void setup() throws Exception {
        restClientService.afterPropertiesSet();
        restClient = restClientService.register(URI.create("https://jsonplaceholder.typicode.com/"), "dummy");
    }

    @Test
    void getPosts() throws IOException {
        PostApi api = restClient.create(PostApi.class);
        Collection<Post> posts = api.list().execute().body();
        assertThat(posts.size()).isGreaterThan(50);
    }

    @Test
    void getPostsDirect() {
        PostApi api = restClient.create(PostApi.class);
        Collection<Post> posts = api.listDirect();
        assertThat(posts.size()).isGreaterThan(50);
    }

    @Test
    void getPost() throws IOException {
        PostApi api = restClient.create(PostApi.class);
        Post post = api.get(1).execute().body();
        assertNotNull(post);
        assertEquals(1, post.getUserId());
        assertThat(post.getTitle()).contains("occaecati");
    }

    @Test
    void getPostDirect() {
        PostApi api = restClient.create(PostApi.class);
        Post post = api.getDirect(1);
        assertNotNull(post);
        assertEquals(1, post.getUserId());
        assertThat(post.getTitle()).contains("occaecati");
    }

    @Test
    void getPostNotFound() {
        PostApi api = restClient.create(PostApi.class);
        assertThatThrownBy(() -> api.get(11111).execute().body())
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create() throws IOException {
        PostApi api = restClient.create(PostApi.class);
        Post post = api.create(new Post().setTitle("abc").setBody("cde").setUserId(1)).execute().body();
        assertNotNull(post);
        assertEquals(101, post.getId());
    }

    @Test
    void update() throws IOException {
        PostApi api = restClient.create(PostApi.class);
        Post post = api.update(1, new Post().setTitle("abc").setBody("cde").setUserId(1)).execute().body();
        assertNotNull(post);
        assertEquals(1, post.getId());
        assertEquals("abc", post.getTitle());
    }

    @Test
    void delete() throws IOException {
        PostApi api = restClient.create(PostApi.class);
        api.delete(1).execute();
    }


}