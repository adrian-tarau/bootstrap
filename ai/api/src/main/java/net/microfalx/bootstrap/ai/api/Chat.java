package net.microfalx.bootstrap.ai.api;

import net.microfalx.lang.Descriptable;
import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;
import net.microfalx.resource.Resource;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An interface for representing a chat session with an AI model.
 */
public interface Chat extends Identifiable<String>, Nameable, Descriptable {

    /**
     * Returns the prompt used to start a chat session.
     *
     * @return a non-null instance
     */
    Prompt getPrompt();

    /**
     * Returns the model used by this chat session.
     *
     * @return a non-null instance
     */
    Model getModel();

    /**
     * Returns the user that created the chat.
     *
     * @return a non-null instance
     */
    Principal getUser();

    /**
     * Returns the start time of chat.
     *
     * @return a non-null instance
     */
    LocalDateTime getStartAt();

    /**
     * Returns the finish time of chat.
     *
     * @return a non-null instance
     */
    LocalDateTime getFinishAt();

    /**
     * Returns duration of the chat.
     *
     * @return a non-null instance
     */
    Duration getDuration();

    /**
     * Returns the system message that provides context or instructions for the chat session.
     * @return a non-null instance
     */
    Message getSystemMessage();

    /**
     * Returns the messages exchanged in the chat.
     *
     * @return a non-null collection of messages
     */
    Collection<Message> getMessages();

    /**
     * Returns the messages exchanged in the chat.
     *
     * @return a non-null collection of messages
     */
    Collection<Message> getMessages(boolean includeSystemMessage);

    /**
     * Returns the number of messages in the chat.
     *
     * @return a positive integer
     */
    int getMessageCount();

    /**
     * Returns the content of the chat in text form.
     *
     * @return a non-null instance
     */
    String getContent();

    /**
     * Returns the token count of the chat.
     *
     * @return a non-null instance
     */
    int getTokenCount();

    /**
     * Returns the tags associated with the chat session.
     *
     * @return a non-null set
     */
    Set<String> getTags();

    /**
     * Updates the chat name to be different from the generated one. If the name is null or empty, it will be ignored.
     *
     * @param name the new name
     */
    void updateName(String name);

    /**
     * Asks a question to the AI model and returns the answer.
     *
     * @param message the message to send to the model
     * @return the response as a
     */
    String ask(String message);

    /**
     * Asks a question to the AI model and returns a stream of tokens.
     *
     * @param message the message to send to the model
     * @return a stream of tokens
     */
    TokenStream chat(String message);

    /**
     * Registers a feature with the chat session. Features can be used to add context to the chat session.
     * <p>
     * If the feature object is null or If the feature already exists, it will be ignored.
     *
     * @param feature the feature to add
     * @param <F>     the feature type
     */
    <F> void addFeature(F feature);

    /**
     * Returns a feature of the specified type from the chat session.
     *
     * @param featureType the type of the feature to retrieve
     * @param <F>         the feature type
     * @return the feature of the specified type, or null if not found
     */
    <F> F getFeature(Class<F> featureType);

    /**
     * Estimatest the number of tokens exchanged with the current model.
     *
     * @param text the text
     * @return the number of tokens
     */
    int getTokenCount(String text);

    /**
     * Returns the time taken to receive the first token in the stream.
     *
     * @return a non-null instance
     */
    Duration getTimeToFirstToken();

    /**
     * Returns the tools available to the chat session.
     *
     * @return a non-null collection
     */
    Collection<Tool> getTools();

    /**
     * Returns the tool executions performed during the chat session.
     *
     * @return a non-null map
     */
    Map<Tool.ExecutionRequest, Tool.ExecutionResponse> getToolExecutions();

    /**
     * Returns the tool execution failures that occurred during the chat session.
     *
     * @return a non-null map
     */
    Map<Tool.ExecutionRequest, Throwable> getToolExecutionFailures();

    /**
     * Returns a Markdown description of the tools available and their invocations in the chat session.
     *
     * @return a non-null string containing the description
     */
    String getToolsDescription();

    /**
     * Returns whether a tool with a given name is registered and enabled in the chat session.
     *
     * @param name the name of the tool to check
     * @return {@code true} if the tool is registered and enabled, {@code false} otherwise
     */
    boolean hasTool(String name);

    /**
     * Adds a tool to the chat session. If a tool with the same name already exists, it will be replaced.
     *
     * @param tool the tool to add
     */
    void addTool(Tool tool);

    /**
     * Disables a tool by its name. If the tool is not found, it will be ignored.
     *
     * @param name the name of the tool to disable
     */
    void disableTool(String name);

    /**
     * Disables all tools associated with this chat session.
     */
    void disableTools();

    /**
     * Returns the variables registered in the chat session. Variables can be used in prompts to provide dynamic content.
     * @return a non-null instance
     */
    Map<String, Object> getVariables();

    /**
     * Registers a variable that can be used in prompts.
     *
     * @param name  the name of the variable
     * @param value the value of the variable
     */
    void addVariable(String name, Object value);

    /**
     * Adds an attribute to the chat session. Attributes can be used to store additional information
     *
     * @param name  the name of the attribute
     * @param value the value of the attribute, can be null
     */
    void addAttribute(String name, Object value);

    /**
     * Returns an attribute by its name. If the attribute is not found, it will return null.
     *
     * @param name the name of the attribute
     * @param <T>  the type of the attribute
     * @return the value of the attribute, or null if not found
     */
    <T> T getAttribute(String name);

    /**
     * Returns whether an attribute with a given name exists in the chat session.
     *
     * @param name the name of the attribute to check
     * @return {@code true} if the attribute exists, {@code false} otherwise
     */
    boolean hasAttribute(String name);

    /**
     * Pings the chat session to keep it active and prevent it from being closed due to inactivity.
     */
    void ping();

    /**
     * Returns the logs of the chat session, including messages, tool executions, and other relevant information.
     * <p>
     * Logs should be formated using Markdown syntax.
     *
     * @return a non-null instance
     */
    Resource getLogs();

    /**
     * Completes the chat session and records the chat in the history.
     */
    void close();

    /**
     * A factory for creating chat sessions.
     */
    interface Factory {

        /**
         * Creates a chat session with a given identifier.
         *
         * @param prompt the prompt to use for the chat
         * @param model the model to use for the chat
         * @return a non-null instance
         */
        Chat createChat(Prompt prompt, Model model);
    }
}
