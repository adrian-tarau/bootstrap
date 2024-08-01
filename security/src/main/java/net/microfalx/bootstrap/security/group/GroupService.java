package net.microfalx.bootstrap.security.group;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.microfalx.bootstrap.security.SecurityConstants;
import net.microfalx.bootstrap.security.user.Role;
import net.microfalx.lang.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.time.Duration.ofSeconds;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Service
public class GroupService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupService.class);

    private static final int DEFAULT_GROUP_COUNT = 2;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private JdbcClient jdbcClient;

    private final Cache<Integer, Set<Role>> cachedRoles = CacheBuilder.newBuilder().expireAfterWrite(ofSeconds(30)).build();

    @Override
    public void afterPropertiesSet() throws Exception {
        createDefaultGroups();
    }

    /**
     * Returns the roles associated with a group.
     *
     * @param group the group identifier
     * @return a non-null instance
     */
    public Set<Role> getRoles(Identifiable<Integer> group) {
        requireNonNull(group);
        Set<Role> roles = cachedRoles.getIfPresent(group.getId());
        if (roles == null) {
            loadRolesInCache();
            roles = cachedRoles.getIfPresent(group.getId());
            if (roles == null) roles = Collections.emptySet();
        }
        return roles;
    }

    /**
     * Changes the roles assigned with a group.
     *
     * @param group the group identifier
     * @param roles the roles
     */
    public void setRoles(Identifiable<Integer> group, Set<Role> roles) {
        requireNonNull(group);
        requireNonNull(roles);
        jdbcClient.sql("delete from  security_group_authorities where group_id= ?").param(1, group.getId())
                .update();
        JdbcClient.StatementSpec statement = jdbcClient.sql("insert into security_group_authorities (group_id, authority) values (?,?)").param(1, group.getId());
        for (Role role : roles) {
            statement.param(2, role.getId());
            statement.update();
        }
        cachedRoles.put(group.getId(), roles);
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

    private void loadRolesInCache() {
        jdbcClient.sql("select * from security_group_authorities")
                .query(this::loadRole);
    }

    private void loadRole(ResultSet rs) throws SQLException {
        Role role = Role.create(rs.getString("authority")).build();
        int groupId = rs.getInt("group_id");
        Set<Role> roles = cachedRoles.getIfPresent(groupId);
        if (roles == null) {
            roles = new LinkedHashSet<>();
            cachedRoles.put(groupId, roles);
        }
        roles.add(role);
    }
}
