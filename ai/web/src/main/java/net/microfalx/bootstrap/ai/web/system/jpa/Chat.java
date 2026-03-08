package net.microfalx.bootstrap.ai.web.system.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.dataset.annotation.OrderBy;
import net.microfalx.bootstrap.jdbc.entity.natural.NamedAndTaggedIdentityAware;
import net.microfalx.bootstrap.jdbc.jpa.DurationConverter;
import net.microfalx.lang.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity(name = "WebChatModel")
@Table(name = "ai_chats")
@Name("Chats")
@Getter
@Setter
@ReadOnly
public class Chat extends NamedAndTaggedIdentityAware<String> {

    @ManyToOne
    @Position(10)
    @JoinColumn(name = "model_id", nullable = false)
    @Description("The model used by this chat session")
    @Width("200px")
    private Model model;

    @Position(15)
    @Column(name = "user_id", nullable = false)
    @Description("The user that created the chat")
    @Width("100px")
    private String user;

    @Column(name = "start_at", nullable = false)
    @Position(20)
    @Description("The start time of chat")
    @OrderBy(OrderBy.Direction.DESC)
    private LocalDateTime startAt;

    @Column(name = "finish_at", nullable = false)
    @Position(21)
    @Description("The finish time of chat")
    @Visible(false)
    private LocalDateTime finishAt;

    @Column(name = "duration", nullable = false)
    @Position(22)
    @Description("The total duration of the chat")
    @Convert(converter = DurationConverter.class)
    @Width("80px")
    private Duration duration;

    @Column(name = "token_count", nullable = false)
    @Position(30)
    @Description("The token count of the chat")
    @Width("100px")
    private int tokenCount;

    @Column(name = "time_to_first_token", nullable = false)
    @Convert(converter = DurationConverter.class)
    @Position(30)
    @Description("The average time between a question being asked and the first token")
    @Width("100px")
    private Duration timeToFirstToken;

    @Column(name = "prompt_uri", nullable = false)
    @Position(30)
    @Description("The content of the chat prompt (system message)")
    @Visible(false)
    private String promptUri;

    @Column(name = "memory_uri", nullable = false)
    @Position(31)
    @Description("The content of the chat memory")
    @Visible(false)
    private String memoryUri;

    @Column(name = "logs_uri", nullable = false)
    @Position(32)
    @Description("The content of the chat logs")
    @Visible(false)
    private String logsUri;

    @Column(name = "tools_uri", nullable = false)
    @Position(33)
    @Description("The tools available of the chat and their usage")
    @Visible(false)
    private String toolsUri;


}
