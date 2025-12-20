package net.microfalx.bootstrap.restapi.client.jsonplaceholder;

import lombok.Data;

@Data
public class Post {
    private int userId;
    private int id;
    private String title;
    private String body;
}
