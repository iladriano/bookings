package com.faex.bookings.rest;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingRequestBody {
  private String email;
  private String fullName;
  private String checkIn;
  private String checkOut;
}
