package dev.sogong_girls.mr_world.domain.souvenir.dto;

import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
                @NotNull Integer amount) {
}
