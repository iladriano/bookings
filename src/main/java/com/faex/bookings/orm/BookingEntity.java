package com.faex.bookings.orm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BookingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long id;
  private long createdTime, updatedTime, deletedTime;
  private String email, fullName, checkIn, checkOut;
  private Status status;
  public enum Status { CONFIRMED, DELETED }
}
