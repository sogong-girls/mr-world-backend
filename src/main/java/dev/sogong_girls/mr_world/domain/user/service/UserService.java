package dev.sogong_girls.mr_world.domain.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.sogong_girls.mr_world.domain.booking.dto.BookingResponse;
import dev.sogong_girls.mr_world.domain.booking.repository.BookingRepository;
import dev.sogong_girls.mr_world.domain.user.dto.UserResponse;
import dev.sogong_girls.mr_world.domain.user.dto.UserUpdateRequest;
import dev.sogong_girls.mr_world.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public UserResponse getUser() {
        throw new UnsupportedOperationException("getUser is not implemented yet");
    }

    public UserResponse updateUser(UserUpdateRequest request) {
        throw new UnsupportedOperationException("updateUser is not implemented yet");
    }

    public List<BookingResponse> getBookingList() {
        throw new UnsupportedOperationException("getBookingList is not implemented yet");
    }
}
