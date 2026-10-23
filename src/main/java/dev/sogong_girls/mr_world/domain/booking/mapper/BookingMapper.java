package dev.sogong_girls.mr_world.domain.booking.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingDetailResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.dto.SmsNotificationResponse;
import dev.sogong_girls.mr_world.domain.booking.entity.Booking;
import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookingMapper {

    @Mapping(target = "tourId", source = "tour.id")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponses(List<Booking> bookings);

    BookingDetailResponse toDetailResponse(Booking booking);

    SmsNotificationResponse toNotificationResponse(SmsNotification notification);

    List<SmsNotificationResponse> toNotificationResponses(List<SmsNotification> notifications);
}
