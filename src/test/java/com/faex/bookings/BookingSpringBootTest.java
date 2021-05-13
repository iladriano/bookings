package com.faex.bookings;

import com.faex.bookings.orm.BookingEntity;
import com.faex.bookings.rest.Availability;
import com.faex.bookings.rest.AvailabilityDate;
import com.faex.bookings.rest.BookingRequestBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingSpringBootTest {
	@Test
	public void testCreateBookingOk() throws Exception {
		postBookingWithHandler(o -> {});
		mockMvc.perform(delete("/dates")); // ensure cleanup
	}

	@Test
	public void testCreateBookingConcurrently() throws Exception {
		var status1 = new AtomicInteger(0);
		var status2 = new AtomicInteger(0);
		new Thread(() -> postBookingWithConsumer(status1::set)).start();
		new Thread(() -> postBookingWithConsumer(status2::set)).start();
		Thread.sleep(1000);
		var statuses = List.of(status1.get(), status2.get());
		Assertions.assertTrue(statuses.contains(400) && statuses.contains(201));
		mockMvc.perform(delete("/dates")); // ensure cleanup
	}

	@Test
	public void testCreateBookingWithGreaterThanThreeDays() throws Exception {
		postBookingWithBadRequest(builder -> builder
			.fullName("Tester")
			.email("tester@testing.test")
			.checkIn(t0.plusDays(1).toString())
			.checkOut(t0.plusDays(5).toString())
		);
	}

	@Test
	public void testCreateBookingWithLessThanOneDay() throws Exception {
		postBookingWithBadRequest(builder -> builder
			.fullName("Tester")
			.email("tester@testing.test")
			.checkIn(t0.plusDays(1).toString())
			.checkOut(t0.plusDays(1).toString())
		);
	}

	@Test
	public void testCreateBookingWithBadDates() throws Exception {
		postBookingWithBadRequest(builder -> builder
			.fullName("Tester")
			.email("tester@testing.test")
			.checkIn(t0.plusDays(1).toString())
			.checkOut(t0.toString())
		);
	}

	@Test
	public void testCreateBookingWithoutEmail() throws Exception {
		postBookingWithBadRequest(builder -> builder
			.fullName("Tester")
			.email(" ")
			.checkIn(t0.plusDays(1).toString())
			.checkOut(t0.plusDays(2).toString())
		);
	}

	@Test
	public void testCreateBookingWithoutFullName() throws Exception {
		postBookingWithBadRequest(builder -> builder
			.email("tester@testing.test")
			.checkIn(t0.plusDays(1).toString())
			.checkOut(t0.plusDays(2).toString())
		);
	}

	@Test
	public void testUpdateBookingOk() throws Exception {
		postBookingWithHandler(entity ->
			mockMvc.perform(put("/bookings/" + entity.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.content(BookingSpringBootTest.this.getValueAsString(BookingRequestBody.builder()
				.fullName("Tester")
				.email("tester@testing.test")
				.checkIn(t0.plusDays(1).toString())
				.checkOut(t0.plusDays(2).toString())
				.build())))
			.andExpect(status().isOk()));
		mockMvc.perform(delete("/dates")); // ensure cleanup
	}

	@Test
	public void testUpdateBookingNotFound() throws Exception {
		mockMvc.perform(put("/bookings/123")
			.contentType(MediaType.APPLICATION_JSON)
			.content(getValueAsString(BookingRequestBody.builder()
			.fullName("Tester")
			.email("tester@testing.test")
			.checkIn(t0.plusDays(1).toString())
			.checkOut(t0.plusDays(2).toString())
			.build())))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void testDeleteBookingOk() throws Exception {
		postBookingWithHandler(entity -> mockMvc.perform(delete("/bookings/" + entity.getId()))
			.andExpect(status().isNoContent()));
	}

	@Test
	public void testDeleteBookingNotFound() throws Exception {
		mockMvc.perform(delete("/bookings/123"))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void testBothDefaultAvailableDates() throws Exception {
		mockMvc.perform(get("/dates"))
			.andExpect(status().isOk())
			.andExpect(content().json(availability));
	}

	@Test
	public void testToDefaultAvailableDates() throws Exception {
		mockMvc.perform(get("/dates?from=" + t0))
			.andExpect(status().isOk())
			.andExpect(content().json(availability));
	}

	@Test
	public void testFromDefaultAvailableDates() throws Exception {
		mockMvc.perform(get("/dates?to=" + t0))
			.andExpect(status().isOk())
			.andExpect(content().json(getValueAsString(Availability.builder()
				.from(String.valueOf(t0))
				.to(String.valueOf(t0.plusDays(1)))
				.count(1)
				.dates(List.of(AvailabilityDate.builder()
					.date(String.valueOf(t0))
					.status(AvailabilityDate.Status.AVAILABLE)
					.build()))
				.build())));
	}

	@Test
	public void testRequestedAvailableDates() throws Exception {
		mockMvc.perform(get("/dates?from=" + t0 + "&to=" + t1))
			.andExpect(status().isOk())
			.andExpect(content().json(availability));
	}

	@Test
	public void testRequestedAvailableDatesWithGaps() throws Exception {
		// create a gap of 3 days
		postBookingWithHandler(o -> {});
		// move to list?
		var date1 = t0.plusDays(1).toString();
		var date2 = t0.plusDays(2).toString();
		var date3 = t0.plusDays(3).toString();
		var bookedAvailability = getValueAsString(Availability.builder()
			.from(String.valueOf(t0))
			.to(String.valueOf(t1))
			.count(dates.size())
			.dates(dates.stream().map(availabilityDate -> AvailabilityDate.builder()
				.date(availabilityDate.getDate())
				.status(availabilityDate.getDate().equals(date1)
					|| availabilityDate.getDate().equals(date2)
					|| availabilityDate.getDate().equals(date3) ? AvailabilityDate.Status.UNAVAILABLE : AvailabilityDate.Status.AVAILABLE)
				.build()).toList())
			.build());
		mockMvc.perform(get("/dates?from=" + t0 + "&to=" + t1))
			.andExpect(status().isOk())
			.andExpect(content().json(bookedAvailability));
		mockMvc.perform(delete("/dates")); // ensure cleanup
	}

	@Test
	public void test() throws Exception {
		mockMvc.perform(get("/test"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]"));
	}

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper mapper = new ObjectMapper();
	private final ZoneId zoneId = ZoneId.of("Atlantic/Bermuda");
	private final LocalDate now = LocalDate.now(zoneId);
	private final LocalDate t0 = now.plusDays(1);
	private final LocalDate t1 = now.plusMonths(1);
	private final List<AvailabilityDate> dates = t0.datesUntil(t1).map(o -> AvailabilityDate.builder().date(String.valueOf(o)).status(AvailabilityDate.Status.AVAILABLE).build()).toList();
	private final String availability = getValueAsString(Availability.builder().from(String.valueOf(t0)).to(String.valueOf(t1)).count(dates.size()).dates(dates).build());

	@SneakyThrows(JsonProcessingException.class)
	private <T> String getValueAsString(T value) {
		return mapper.writeValueAsString(value);
	}

	interface BookingEntityHandler {
		void handle(BookingEntity bookingEntity) throws Exception;
	}

	private ResultActions postBooking(Consumer<BookingRequestBody.BookingRequestBodyBuilder> consumer) throws Exception {
		var builder = BookingRequestBody.builder();
		consumer.accept(builder);
		return mockMvc.perform(post("/bookings")
			.contentType(MediaType.APPLICATION_JSON)
			.content(getValueAsString(builder.build())));
	}

	private ResultActions postBooking() throws Exception {
		return postBooking(builder -> builder
			.fullName("Tester")
			.email("tester@testing.test")
			.checkIn(t0.plusDays(1).toString())
			.checkOut(t0.plusDays(4).toString()) // booking 3 days
		);
	}

	private void postBookingWithBadRequest(Consumer<BookingRequestBody.BookingRequestBodyBuilder> consumer) throws Exception {
		postBooking(consumer)
			.andExpect(status().isBadRequest());
	}

	private void postBookingWithHandler(BookingEntityHandler handler) throws Exception {
		postBooking()
			.andDo(o -> handler.handle(mapper.readValue(o.getResponse().getContentAsString(), BookingEntity.class)))
			.andExpect(status().isCreated());
	}

	@SneakyThrows
	private void postBookingWithConsumer(Consumer<Integer> consumer) {
		postBooking()
			.andDo(o -> consumer.accept(o.getResponse().getStatus()));
	}
}