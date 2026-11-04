package dev.sogong_girls.mr_world.domain.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.entity.SmsNotification;
import dev.sogong_girls.mr_world.domain.booking.mapper.BookingMapper;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import dev.sogong_girls.mr_world.domain.booking.repository.SmsNotificationRepository;
import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import dev.sogong_girls.mr_world.domain.user.dto.UserUpdateRequest;
import dev.sogong_girls.mr_world.domain.user.entity.User;
import dev.sogong_girls.mr_world.domain.user.mapper.UserMapper;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final SmsNotificationRepository smsNotificationRepository;

    private final UserMapper userMapper;
    private final BookingMapper bookingMapper;

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.NOT_FOUND));
        String prevPhoneNumber = user.getPhoneNumber();

        user.updateInfo(request.name(), request.phoneNumber(), request.address());

        if (!user.getPhoneNumber().equals(prevPhoneNumber)) {
            // 유저 전화번호가 바뀌면 진행 중인 예약의 Notification도 전송할 전화번호 변경
            List<SmsNotification> unsentNotifications = smsNotificationRepository
                    .findUnsentByPhoneNumber(prevPhoneNumber);
            for (SmsNotification s : unsentNotifications) {
                s.updatePhoneNumber(user.getPhoneNumber());
            }
        }

        return userMapper.toResponse(user);
    }

    public List<BookingResponse> getBookingList(Long id) {
        return bookingMapper.toResponses(bookingRepository.findAllByUserId(id));
    }
}
