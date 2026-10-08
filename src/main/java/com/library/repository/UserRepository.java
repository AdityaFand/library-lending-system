package com.library.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.entity.User;
import com.library.enums.Role;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	Page<User> findByRole(Role role, Pageable pageable);

	@Query("""
			select u from User u
			where u.role = :role
			  and (lower(u.name) like :pattern escape '\\' or lower(u.email) like :pattern escape '\\')
			""")
	Page<User> searchByRole(@Param("role") Role role, @Param("pattern") String pattern, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from User u where u.id = :id")
	Optional<User> findByIdForUpdate(@Param("id") Long id);
}
