package com.faex.bookings.rest;

import com.faex.bookings.orm.BookingEntity;
import com.faex.bookings.service.Booking;
import com.faex.bookings.service.BookingRange;
import com.faex.bookings.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/")
public record BookingRestController(BookingService bookingService) {

  @GetMapping(value = "/dates")
  public Availability getAvailability(String from, String to) {
    return bookingService.getAvailability(buildBookingRange(from, to));
  }

  @PostMapping(value = "/bookings")
  @ResponseStatus(HttpStatus.CREATED)
  public BookingEntity createBooking(@RequestBody BookingRequestBody body) {
    try {
      return bookingService.createBooking(buildBooking(body));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create booking", e);
    }
  }

  @PutMapping(value = "/bookings/{id}")
  public BookingEntity updateBooking(@PathVariable String id, @RequestBody BookingRequestBody body) {
    try {
      return bookingService.updateBooking(id, buildBooking(body));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to update booking", e);
    }
  }

  private BookingRange buildBookingRange(String from, String to) {
    try {
      var zoneId = ZoneId.of("Atlantic/Bermuda");
      var now = LocalDate.now(zoneId);
      var t0 = now.plusDays(1);
      var t1 = now.plusMonths(1);
      var fromDate = from == null || from.isBlank() ? t0 : LocalDate.parse(from, DateTimeFormatter.ISO_LOCAL_DATE);
      var toDate = to == null || to.isBlank() ? t1 : LocalDate.parse(to, DateTimeFormatter.ISO_LOCAL_DATE);
      if (fromDate.isBefore(t0)) {
        throw new IllegalArgumentException("From-date must be >= today + 1 day");
      }
      if (fromDate.isAfter(t1)) {
        throw new IllegalArgumentException("From-date must be <= today + 1 month");
      }
      if (fromDate.isAfter(toDate)) {
        throw new IllegalArgumentException("To-date required to be after from-date");
      }
      if (toDate.isAfter(t1)) {
        throw new IllegalArgumentException("To-date must be <= today + 1 month");
      }
      if (fromDate.isEqual(toDate)) {
        toDate = toDate.plusDays(1);
      }
      return BookingRange.builder()
        .dates(fromDate.datesUntil(toDate).map(String::valueOf).toList())
        .from(fromDate.toString())
        .to(toDate.toString())
        .build();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad parameters", e);
    }
  }

  private Booking buildBooking(BookingRequestBody bookingRequestBody) {
    try {
      var checkIn = LocalDate.parse(bookingRequestBody.getCheckIn());
      var checkOut = LocalDate.parse(bookingRequestBody.getCheckOut());
      if (!checkOut.isAfter(checkIn)) {
        throw new IllegalArgumentException("Check-in needs to be before check-out");
      }
      var dates = checkIn.datesUntil(checkOut).map(LocalDate::toString).toList();
      if (dates.size() > 3 || dates.size() < 1) {
        throw new IllegalArgumentException("Stay needs to be between 1 up to 3 days");
      }
      if (bookingRequestBody.getFullName().isBlank()) {
        throw new IllegalArgumentException("Full name required");
      }
      if (bookingRequestBody.getEmail().isBlank()) {
        throw new IllegalArgumentException("Email required");
      }
      return Booking.builder()
        .dates(dates)
        .checkIn(checkIn.toString())
        .checkOut(checkOut.toString())
        .email(bookingRequestBody.getEmail())
        .fullName(bookingRequestBody.getFullName())
        .build();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad parameters", e);
    }
  }

  @DeleteMapping(value = "/bookings/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteBooking(@PathVariable String id) {
    try {
      bookingService.deleteBooking(id);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to delete booking", e);
    }
  }

  @DeleteMapping(value = "/dates")
  public void deleteDates() {
    bookingService.deleteDates();
  }

  @GetMapping(value = "/test")
  public List<BookingRequestBody> test() {
    return Collections.emptyList();
  }

}
