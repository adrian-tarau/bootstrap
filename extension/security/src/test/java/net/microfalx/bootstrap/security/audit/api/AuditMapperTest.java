package net.microfalx.bootstrap.security.audit.api;

import net.microfalx.bootstrap.restapi.RestApiMapper;
import net.microfalx.bootstrap.security.audit.jpa.Audit;
import net.microfalx.bootstrap.security.group.api.GroupDTO;
import net.microfalx.bootstrap.security.group.jpa.Group;
import net.microfalx.bootstrap.security.user.Role;
import net.microfalx.bootstrap.security.user.api.UserDto;
import net.microfalx.bootstrap.security.user.jpa.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuditMapperTest {

    private RestApiMapper<Audit, AuditDTO> mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuditMapper();
    }

    @Test
    void toEntity() {
        AuditDTO auditDTO = createAuditDTO();
        Audit audit = mapper.toEntity(auditDTO);
        assertEquals(auditDTO.getAction(), audit.getAction());
        assertEquals(auditDTO.getModule(), audit.getModule());
        assertEquals(auditDTO.getCategory(), audit.getCategory());
        assertEquals(auditDTO.getClientInfo(), audit.getClientInfo());
        assertEquals(auditDTO.getReference(), audit.getReference());
        assertEquals(auditDTO.getErrorCode(), audit.getErrorCode());
        assertEquals(auditDTO.getDescription(), audit.getDescription());
    }

    @Test
    void toDTO() {
        Audit audit = createAudit();
        AuditDTO auditDTO = mapper.toDto(audit);
        assertEquals(audit.getAction(), auditDTO.getAction());
        assertEquals(audit.getModule(), auditDTO.getModule());
        assertEquals(audit.getCategory(), auditDTO.getCategory());
        assertEquals(audit.getClientInfo(), auditDTO.getClientInfo());
        assertEquals(audit.getReference(), auditDTO.getReference());
        assertEquals(audit.getErrorCode(), auditDTO.getErrorCode());
        assertEquals(audit.getDescription(), auditDTO.getDescription());
    }

    private AuditDTO createAuditDTO() {
        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setAction("Sample Action");
        auditDTO.setModule("Sample Module");
        auditDTO.setCategory("Sample Category");
        auditDTO.setClientInfo("Sample Client Info");
        auditDTO.setReference("Sample Reference");
        auditDTO.setErrorCode("Sample Error Code");
        auditDTO.setDescription("A sample audit record for testing");
        return auditDTO;
    }

    private Audit createAudit() {
        Audit audit = new Audit();
        audit.setAction("Sample Action");
        audit.setModule("Sample Module");
        audit.setCategory("Sample Category");
        audit.setClientInfo("Sample Client Info");
        audit.setReference("Sample Reference");
        audit.setErrorCode("Sample Error Code");
        audit.setDescription("A sample audit record for testing");
        return audit;
    }

}