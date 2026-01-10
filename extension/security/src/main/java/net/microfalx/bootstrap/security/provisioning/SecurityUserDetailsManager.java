package net.microfalx.bootstrap.security.provisioning;

import lombok.Getter;
import lombok.Setter;
import net.microfalx.bootstrap.security.user.Role;
import net.microfalx.bootstrap.web.util.ExtendedUserDetails;
import net.microfalx.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import static net.microfalx.lang.StringUtils.toLowerCase;

/**
 * The application user details.
 */
public class SecurityUserDetailsManager extends JdbcUserDetailsManager {

    SecurityUserDetailsManager(DataSource dataSource) {
        super(dataSource);
        updateSqlStatements();
    }

    @Override
    protected void addCustomAuthorities(String username, List<GrantedAuthority> authorities) {
        super.addCustomAuthorities(username, authorities);
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(Role.GUEST.getId()));
        }
    }

    @Override
    protected List<UserDetails> loadUsersByUsername(String username) {
        return getJdbcTemplate().query(getUsersByUsernameQuery(), this::mapToUser, username, toLowerCase(username));
    }

    protected List<GrantedAuthority> loadUserAuthorities(String username) {
        return getJdbcTemplate().query(this.getAuthoritiesByUsernameQuery(), new String[]{username, toLowerCase(username)}, (rs, rowNum) -> {
            String roleName = getRolePrefix() + rs.getString(2);
            return new SimpleGrantedAuthority(roleName);
        });
    }

    @Override
    protected UserDetails createUserDetails(String username, UserDetails userFromUserQuery, List<GrantedAuthority> combinedAuthorities) {
        ExtendedUserDetails extendedUserFromUserQuery = (ExtendedUserDetails) userFromUserQuery;
        ExtendedUserDetailsImpl user = new ExtendedUserDetailsImpl(userFromUserQuery.getUsername(), userFromUserQuery.getPassword(), userFromUserQuery.isEnabled(),
                userFromUserQuery.isAccountNonExpired(), userFromUserQuery.isCredentialsNonExpired(),
                userFromUserQuery.isAccountNonLocked(), combinedAuthorities);
        user.setName(extendedUserFromUserQuery.getUsername());
        user.setDisplayName(extendedUserFromUserQuery.getName());
        user.setEmail(extendedUserFromUserQuery.getEmail());
        return user;
    }

    private UserDetails mapToUser(ResultSet rs, int rowNum) throws SQLException {
        String userName = rs.getString(1);
        String password = rs.getString(2);
        String name = rs.getString(3);
        String email = rs.getString(4);
        boolean enabled = rs.getBoolean(5);
        boolean external = rs.getBoolean(6);
        boolean resetPassword = rs.getBoolean(7);
        boolean accLocked = false;
        boolean accExpired = false;
        boolean credsExpired = false;
        if (rs.getMetaData().getColumnCount() > 8) {
            // NOTE: acc_locked, acc_expired and creds_expired are also to be loaded
            accLocked = rs.getBoolean(8);
            accExpired = rs.getBoolean(9);
            credsExpired = rs.getBoolean(10);
        }
        ExtendedUserDetailsImpl user = new ExtendedUserDetailsImpl(userName, password, enabled, !accExpired, !credsExpired, !accLocked,
                AuthorityUtils.NO_AUTHORITIES);
        user.setName(name);
        user.setEmail(email);
        user.setExternal(external);
        user.setResetPassword(resetPassword);
        return user;
    }

    private void updateSqlStatements() {
        setUsersByUsernameQuery(DEF_USERS_BY_USERNAME_QUERY);
        setAuthoritiesByUsernameQuery(DEF_AUTHORITIES_BY_USERNAME_QUERY);
        setGroupAuthoritiesByUsernameQuery(DEF_GROUP_AUTHORITIES_BY_USERNAME_QUERY);

        setCreateUserSql(DEF_CREATE_USER_SQL);
        setUpdateUserSql(DEF_DELETE_USER_SQL);
        setDeleteUserSql(DEF_UPDATE_USER_SQL);
        setCreateAuthoritySql(DEF_INSERT_AUTHORITY_SQL);
        setDeleteUserAuthoritiesSql(DEF_DELETE_USER_AUTHORITIES_SQL);
        setUserExistsSql(DEF_USER_EXISTS_SQL);
        setChangePasswordSql(DEF_CHANGE_PASSWORD_SQL);

        setInsertGroupSql(DEF_INSERT_GROUP_SQL);
        setFindGroupIdSql(DEF_FIND_GROUP_ID_SQL);
        setFindGroupIdSql(DEF_FIND_GROUP_ID_SQL);
        setFindAllGroupsSql(DEF_FIND_GROUPS_SQL);
        setInsertGroupAuthoritySql(DEF_INSERT_GROUP_AUTHORITY_SQL);
        setDeleteGroupSql(DEF_DELETE_GROUP_SQL);
        setRenameGroupSql(DEF_RENAME_GROUP_SQL);
        setInsertGroupAuthoritySql(DEF_GROUP_AUTHORITIES_QUERY_SQL);
        setDeleteGroupAuthoritiesSql(DEF_DELETE_GROUP_AUTHORITIES_SQL);
        setDeleteGroupMembersSql(DEF_DELETE_GROUP_MEMBERS_SQL);
        setDeleteGroupMemberSql(DEF_DELETE_GROUP_MEMBER_SQL);
        setDeleteGroupAuthoritySql(DEF_DELETE_GROUP_AUTHORITY_SQL);
        setInsertGroupMemberSql(DEF_INSERT_GROUP_MEMBER_SQL);
        setFindUsersInGroupSql(DEF_FIND_USERS_IN_GROUP_SQL);
    }

    @Setter
    @Getter
    static class ExtendedUserDetailsImpl extends User implements ExtendedUserDetails {

        private String displayName;
        private String name;
        private String email;
        private String imageUrl;
        private boolean external;
        private boolean resetPassword;

        public ExtendedUserDetailsImpl(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
            super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        }

        public String getDisplayName() {
            return StringUtils.defaultIfEmpty(displayName, getName());
        }

        public ExtendedUserDetailsImpl setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private static final String DEF_CREATE_USER_SQL = "insert into security_users (username, password, name, email, enabled) values (?,?,?)";
    private static final String DEF_DELETE_USER_SQL = "delete from security_users where username = ?";
    private static final String DEF_UPDATE_USER_SQL = "update security_users set password = ?, enabled = ? where username = ?";
    private static final String DEF_INSERT_AUTHORITY_SQL = "insert into security_authorities (username, authority) values (?,?)";
    private static final String DEF_DELETE_USER_AUTHORITIES_SQL = "delete from security_authorities where username = ?";
    private static final String DEF_USER_EXISTS_SQL = "select username from security_users where username = ?";
    private static final String DEF_CHANGE_PASSWORD_SQL = "update security_users set password = ? where username = ?";

    private static final String DEF_USERS_BY_USERNAME_QUERY = "select username, password, name, email, enabled, external, reset_password "
            + "from security_users where username = ? or lower(email) = ?";
    private static final String DEF_AUTHORITIES_BY_USERNAME_QUERY = "select username, authority "
            + "from security_authorities where username = ? or lower(email) = ?";
    private static final String DEF_GROUP_AUTHORITIES_BY_USERNAME_QUERY = "select g.id, g.name, ga.authority "
            + "from security_groups g, security_group_members gm, security_group_authorities ga "
            + "where gm.username = ? and g.id = ga.group_id and g.id = gm.group_id";

    private static final String DEF_FIND_GROUPS_SQL = "select name from security_groups";
    private static final String DEF_FIND_USERS_IN_GROUP_SQL = "select username from security_group_members gm, security_groups g "
            + "where gm.group_id = g.id and g.name = ?";
    private static final String DEF_INSERT_GROUP_SQL = "insert into security_groups (name) values (?)";
    private static final String DEF_FIND_GROUP_ID_SQL = "select id from security_groups where name = ?";
    private static final String DEF_INSERT_GROUP_AUTHORITY_SQL = "insert into security_group_authorities (group_id, authority) values (?,?)";
    private static final String DEF_DELETE_GROUP_SQL = "delete from security_groups where id = ?";
    private static final String DEF_DELETE_GROUP_AUTHORITIES_SQL = "delete from security_group_authorities where group_id = ?";
    private static final String DEF_DELETE_GROUP_MEMBERS_SQL = "delete from security_group_members where group_id = ?";
    private static final String DEF_RENAME_GROUP_SQL = "update security_groups set name = ? where name = ?";
    private static final String DEF_INSERT_GROUP_MEMBER_SQL = "insert into security_group_members (group_id, username) values (?,?)";
    private static final String DEF_DELETE_GROUP_MEMBER_SQL = "delete from security_group_members where group_id = ? and username = ?";
    private static final String DEF_GROUP_AUTHORITIES_QUERY_SQL = "select g.id, g.name, ga.authority "
            + "from security_groups g, security_group_authorities ga " + "where g.name = ? " + "and g.id = ga.group_id ";
    private static final String DEF_DELETE_GROUP_AUTHORITY_SQL = "delete from security_group_authorities where group_id = ? and authority = ?";
}
