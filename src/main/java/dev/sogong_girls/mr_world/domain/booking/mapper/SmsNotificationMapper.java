package dev.sogong_girls.mr_world.domain.booking.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import dev.sogong_girls.mr_world.domain.booking.dto.SmsNotificationResponse;
import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SmsNotificationMapper {

    SmsNotificationResponse toResponse(SmsNotification notification);

    List<SmsNotificationResponse> toResponses(List<SmsNotification> notifications);
}
