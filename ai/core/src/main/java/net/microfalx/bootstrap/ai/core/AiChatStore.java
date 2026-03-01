package net.microfalx.bootstrap.ai.core;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

class AiChatStore implements ChatMemoryRepository {

    private final AiPersistence persistence;
    private final InMemoryChatMemoryRepository memoryRepository = new InMemoryChatMemoryRepository();

    AiChatStore(AiPersistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public List<String> findConversationIds() {
        return memoryRepository.findConversationIds();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return memoryRepository.findByConversationId(conversationId);
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        memoryRepository.saveAll(conversationId, messages);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        memoryRepository.deleteByConversationId(conversationId);
    }
}
