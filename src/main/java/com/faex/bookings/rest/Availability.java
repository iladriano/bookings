package com.faex.bookings.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Availability {
  private String from, to;
  private List<AvailabilityDate> dates;
  private int count;
}
