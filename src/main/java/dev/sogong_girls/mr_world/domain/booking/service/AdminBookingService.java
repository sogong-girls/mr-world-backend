package dev.sogong_girls.mr_world.domain.booking.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingStatusUpdateRequest;
import dev.sogong_girls.mr_world.domain.booking.dto.SmsNotificationResponse;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.mapper.BookingMapper;
import dev.sogong_girls.mr_world.domain.booking.mapper.SmsNotificationMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import dev.sogong_girls.mr_world.domain.booking.repository.SmsNotificationRepository;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBookingService {
    private final BookingRepository bookingRepository;
    private final SmsNotificationRepository smsNotificationRepository;
    private final BookingMapper bookingMapper;
    private final SmsNotificationMapper smsNotificationMapper;

    public List<BookingResponse> getAllBookingList() {
        return bookingMapper.toResponses(bookingRepository.findAll());
    }

    public BookingDetailResponse getAdminBooking(Long id) {
        Booking booking = bookingRepository.findWithOptionsById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        return bookingMapper.toDetailResponse(booking);
    }

    @Transactional
    public BookingResponse updateAdminBookingStatus(Long id, BookingStatusUpdateRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        booking.updateStatus(request.status());
        return bookingMapper.toResponse(booking);
    }

    public List<SmsNotificationResponse> getSmsNotificationList() {
        return smsNotificationMapper.toResponses(smsNotificationRepository.findAll());
    }
}
