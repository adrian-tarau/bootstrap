package net.microfalx.bootstrap.ai.llama;

import net.microfalx.bootstrap.ai.api.Chat;
import net.microfalx.bootstrap.ai.api.Model;
import net.microfalx.bootstrap.ai.api.Prompt;
import net.microfalx.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class LlamaServerTest {

    private Model model;
    private Chat chat;

    @BeforeEach
    void setup() {
        model = Model.create( "Qwen2.5 (0.5b)", "qwen2.5:0.5b")
                .maximumContextLength(32_000)
                .downloadUri("https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true")
                .build();
        chat = new LlamaChat(Prompt.empty(), model);
    }

    @Test
    void ask() throws IOException, InterruptedException {
        LlamaServer server = LlamaServerFactory.getInstance().start(chat);
        chat(server, "Tell me a joke about Java?");
        server.stop();
    }

    private void chat(LlamaServer server, String message) throws IOException, InterruptedException {
        String json = """
                {
                  "model": "local-model",
                  "messages": [
                    {"role": "user", "content": "${message}"}
                  ]
                }
                """;
        json = StringUtils.replaceFirst(json, "${message}", message);
        URI uri = URI.create(server.getUri() + "/chat/completions");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
    }

}