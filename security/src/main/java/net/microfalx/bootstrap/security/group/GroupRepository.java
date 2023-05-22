package net.microfalx.bootstrap.security.group;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface GroupRepository extends JpaRepository<Group, Integer> {
}
