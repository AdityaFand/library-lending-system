package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	boolean existsByBookId(Long bookId);
}
