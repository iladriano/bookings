package com.faex.bookings.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityDate {
  private String date;
  private Status status;
  public enum Status { AVAILABLE, UNAVAILABLE }
}
