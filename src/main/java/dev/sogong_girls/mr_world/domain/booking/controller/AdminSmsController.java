package dev.sogong_girls.mr_world.domain.booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sogong_girls.mr_world.domain.booking.dto.SmsNotificationResponse;
import dev.sogong_girls.mr_world.domain.booking.service.AdminSmsService;

@RestController
public class AdminSmsController {
    private final AdminSmsService service;

    public AdminSmsController(AdminSmsService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/sms-notifications")
    public List<SmsNotificationResponse> getSmsNotificationList() {
        return service.getSmsNotificationList();
    }
}
