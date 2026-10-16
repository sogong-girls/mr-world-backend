package dev.sogong_girls.mr_world.domain.booking.service;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingCreateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingOptionUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingAddonRepository;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingAddonRepository bookingAddonRepository;

    public BookingDetailResponse createBooking(BookingCreateRequest request) {
        throw new UnsupportedOperationException("createBooking is not implemented yet");
    }

    public BookingDetailResponse updateBookingOptions(Long id, BookingOptionUpdateRequest request) {
        throw new UnsupportedOperationException("updateBookingOptions is not implemented yet");
    }

    public BookingResponse payBooking(Long id) {
        throw new UnsupportedOperationException("payBooking is not implemented yet");
    }

    private boolean validateBooking(Long id) {
        throw new UnsupportedOperationException("validateBooking is not implemented yet");
    }

    private boolean checkPayable(Long id) {
        throw new UnsupportedOperationException("checkPayable is not implemented yet");
    }

    private boolean processPayment(Long id) {
        throw new UnsupportedOperationException("processPayment is not implemented yet");
    }
}
