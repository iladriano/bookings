package com.faex.bookings.orm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingDateRepository extends JpaRepository<BookingDateEntity, String> {
  List<BookingDateEntity> findAllByBookingId(long bookingId);
  List<BookingDateEntity> findAllByIdBetween(String from, String to);
}
