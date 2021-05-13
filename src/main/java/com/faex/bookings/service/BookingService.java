package com.faex.bookings.service;

import com.faex.bookings.orm.BookingDateEntity;
import com.faex.bookings.orm.BookingDateRepository;
import com.faex.bookings.orm.BookingEntity;
import com.faex.bookings.orm.BookingRepository;
import com.faex.bookings.rest.Availability;
import com.faex.bookings.rest.AvailabilityDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingService {

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public BookingEntity createBooking(Booking booking) {
    validateBookingDates(booking);
    var bookingEntity = bookingRepository.save(BookingEntity.builder()
      .createdTime(System.nanoTime())
      .status(BookingEntity.Status.CONFIRMED)
      .checkIn(booking.getCheckIn())
      .checkOut(booking.getCheckOut())
      .email(booking.getEmail())
      .fullName(booking.getFullName())
      .build());
    booking.getDates().forEach(date -> dateRepository.save(BookingDateEntity.builder()
      .id(date)
      .bookingId(bookingEntity.getId())
      .build()));
    return bookingEntity;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public BookingEntity updateBooking(String id, Booking booking) {
    var bookingEntity = getBookingEntity(id);
    if (!(Objects.equals(bookingEntity.getEmail(), booking.getEmail()) && Objects.equals(bookingEntity.getFullName(), booking.getFullName()))) {
      throw new IllegalArgumentException("Email and full name don't match existing record");
    }
    deleteBookingDates(bookingEntity);
    validateBookingDates(booking);
    var updatedBookingEntity = bookingRepository.save(bookingEntity
      .setUpdatedTime(System.nanoTime())
      .setCheckIn(booking.getCheckIn())
      .setCheckOut(booking.getCheckOut()));
    booking.getDates().forEach(date -> dateRepository.save(BookingDateEntity.builder()
      .id(date)
      .bookingId(updatedBookingEntity.getId())
      .build()));
    return updatedBookingEntity;
  }

  public void deleteBooking(String id) {
    var bookingEntity = getBookingEntity(id);
    bookingRepository.save(bookingEntity
      .setDeletedTime(System.nanoTime())
      .setStatus(BookingEntity.Status.DELETED));
    deleteBookingDates(bookingEntity);
  }

  public Availability getAvailability(BookingRange bookingRange) {
    var dateSet = dateRepository.findAllByIdBetween(bookingRange.getFrom(), bookingRange.getTo()).stream()
      .map(BookingDateEntity::getId).collect(Collectors.toSet());
    var dates = bookingRange.getDates().stream().map(date -> AvailabilityDate.builder()
      .status(dateSet.contains(date)? AvailabilityDate.Status.UNAVAILABLE : AvailabilityDate.Status.AVAILABLE)
      .date(date)
      .build()).toList();
    return Availability.builder()
      .from(bookingRange.getFrom())
      .to(bookingRange.getTo())
      .count(dates.size())
      .dates(dates)
      .build();
  }

  public void deleteDates() {
    dateRepository.deleteAll();
  }

  @Autowired
  private final BookingRepository bookingRepository;

  @Autowired
  private final BookingDateRepository dateRepository;

  public BookingService(BookingRepository bookingRepository, BookingDateRepository dateRepository) {
    this.bookingRepository = bookingRepository;
    this.dateRepository = dateRepository;
  }

  private BookingEntity getBookingEntity(String id) {
    return bookingRepository.findById(Long.valueOf(id)).orElseThrow(() -> new IllegalArgumentException("Id not found"));
  }

  private void deleteBookingDates(BookingEntity bookingEntity) {
    dateRepository.findAllByBookingId(bookingEntity.getId()).forEach(o -> dateRepository.deleteById(o.getId()));
  }

  private void validateBookingDates(Booking booking) {
    if (!dateRepository.findAllById(booking.getDates()).isEmpty()) {
      throw new IllegalArgumentException("Dates are not available");
    }
  }
}
