package dev.sogong_girls.mr_world.domain.booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingStatusUpdateRequest;

@Service
public class AdminBookingService {
    public List<BookingResponse> getAllBookingList() {
        throw new UnsupportedOperationException("getAllBookingList is not implemented yet");
    }

    public BookingDetailResponse getAdminBooking(Long id) {
        throw new UnsupportedOperationException("getAdminBooking is not implemented yet");
    }

    public BookingResponse updateAdminBookingStatus(Long id, BookingStatusUpdateRequest request) {
        throw new UnsupportedOperationException("updateAdminBookingStatus is not implemented yet");
    }
}
