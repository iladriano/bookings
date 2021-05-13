package com.faex.bookings.orm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking_date", indexes = {@Index(name = "booking_id_index", columnList="booking_id", unique = false)})
public class BookingDateEntity {
  @Id
  private String id;
  @Column(name = "booking_id", nullable = false)
  private long bookingId;
}
