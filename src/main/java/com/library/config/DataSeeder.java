package com.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.library.entity.User;
import com.library.enums.Role;
import com.library.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.seed.librarian.name}")
	private String librarianName;

	@Value("${app.seed.librarian.email}")
	private String librarianEmail;

	@Value("${app.seed.librarian.password}")
	private String librarianPassword;

	@Override
	public void run(String... args) {
		if (userRepository.existsByEmail(librarianEmail)) {
			return;
		}

		User librarian = new User();
		librarian.setName(librarianName);
		librarian.setEmail(librarianEmail);
		librarian.setPassword(passwordEncoder.encode(librarianPassword));
		librarian.setRole(Role.LIBRARIAN);
		userRepository.save(librarian);

		log.info("Default librarian account created: {}", librarianEmail);
	}
}
