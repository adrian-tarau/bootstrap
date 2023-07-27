package net.microfalx.bootstrap.security.group;

import net.microfalx.bootstrap.security.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@Service
public class GroupService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);

    private static final int DEFAULT_GROUP_COUNT = 2;

    @Autowired
    private GroupRepository groupRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        createDefaultGroups();
    }

    private void createDefaultGroups() {
        if (groupRepository.count() >= DEFAULT_GROUP_COUNT) return;
        createGroup("Administrators", "The administrators group", Set.of(SecurityConstants.ADMIN_ROLE));
        createGroup("Users", "Default group for all users without permissions (or minimal permissions)", Collections.emptySet());
    }

    private void createGroup(String name, String description, Set<String> roles) {
        try {
            Group group = persistGroup(name, description);
        } catch (Exception e) {
            LOGGER.error("Failed to register default group for " + name, e);
        }
    }

    private Group persistGroup(String name, String description) {
        Group group = groupRepository.findByName(name);
        if (group != null) return group;
        group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setEnabled(true);
        group.setCreatedAt(LocalDateTime.now());
        groupRepository.saveAndFlush(group);
        return group;
    }
}
