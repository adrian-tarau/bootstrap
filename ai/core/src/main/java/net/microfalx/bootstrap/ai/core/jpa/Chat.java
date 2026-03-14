package net.microfalx.bootstrap.ai.core.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.jdbc.entity.natural.NamedAndTaggedIdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.DurationConverter;
import net.microfalx.lang.annotation.Name;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "CoreChatModel")
@Table(name = "ai_chats")
@Name("Chats")
@Getter
@Setter
public class Chat extends NamedAndTaggedIdentityAware<String> {

    @Column(name = "user_id", nullable = false)
    private String user;

    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @ManyToOne
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "finish_at", nullable = false)
    private LocalDateTime finishAt;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @Column(name = "time_to_first_token", nullable = false)
    @Convert(converter = DurationConverter.class)
    private Duration timeToFirstToken;

    @Column(name = "duration", nullable = false)
    @Convert(converter = DurationConverter.class)
    private Duration duration;

    @Column(name = "prompt_uri", nullable = false)
    private String promptUri;

    @Column(name = "memory_uri", nullable = false)
    private String memoryUri;

    @Column(name = "logs_uri", nullable = false)
    private String logsUri;

    @Column(name = "tools_uri", nullable = false)
    private String toolsUri;
}
