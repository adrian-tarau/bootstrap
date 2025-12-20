package net.microfalx.bootstrap.restapi.client;

import net.microfalx.bootstrap.restapi.client.exception.NotFoundException;
import net.microfalx.bootstrap.restapi.client.jsonplaceholder.Post;
import net.microfalx.bootstrap.restapi.client.jsonplaceholder.PostApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

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
        restClient = restClientService.register(URI.create("https://jsonplaceholder.typicode.com/"), "sasasa");
    }

    @Test
    void getPosts() {
        PostApi api = restClient.create(PostApi.class);
        Collection<Post> posts = restClient.execute(api.list());
        assertThat(posts.size()).isGreaterThan(50);
    }

    @Test
    void getPost() {
        PostApi api = restClient.create(PostApi.class);
        Post post = restClient.execute(api.get(1));
        assertNotNull(post);
        assertEquals(1, post.getUserId());
        assertThat(post.getTitle()).contains("occaecati");
    }

    @Test
    void getPostNotFound() {
        PostApi api = restClient.create(PostApi.class);
        assertThatThrownBy(() -> restClient.execute(api.get(11111)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create() {
        PostApi api = restClient.create(PostApi.class);
        Post post = restClient.execute(api.create(new Post().setTitle("abc").setBody("cde").setUserId(1)));
        assertNotNull(post);
        assertEquals(101, post.getId());
    }

    @Test
    void update() {
        PostApi api = restClient.create(PostApi.class);
        Post post = restClient.execute(api.update(1, new Post().setTitle("abc").setBody("cde").setUserId(1)));
        assertNotNull(post);
        assertEquals(1, post.getId());
        assertEquals("abc", post.getTitle());
    }

    @Test
    void delete() {
        PostApi api = restClient.create(PostApi.class);
        restClient.execute(api.delete(1));
    }


}