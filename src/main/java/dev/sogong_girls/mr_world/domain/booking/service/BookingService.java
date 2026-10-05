package dev.sogong_girls.mr_world.domain.booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingCreateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingOptionUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingStatusUpdateRequest;

@Service
public class BookingService {
    public BookingDetailResponse createBooking(BookingCreateRequest request) {
        throw new UnsupportedOperationException("createBooking is not implemented yet");
    }

    public List<BookingResponse> getBookingList() {
        throw new UnsupportedOperationException("getBookingList is not implemented yet");
    }

    public BookingDetailResponse getBooking(Long id) {
        throw new UnsupportedOperationException("getBooking is not implemented yet");
    }

    public BookingDetailResponse updateBookingOptions(Long id, BookingOptionUpdateRequest request) {
        throw new UnsupportedOperationException("updateBookingOptions is not implemented yet");
    }

    public BookingResponse updateBookingStatus(Long id, BookingStatusUpdateRequest request) {
        throw new UnsupportedOperationException("updateBookingStatus is not implemented yet");
    }
}
