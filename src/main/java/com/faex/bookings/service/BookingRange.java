package com.faex.bookings.service;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingRange {
  private List<String> dates;
  private String from, to;
}
