package dev.sogong_girls.mr_world.domain.souvenir.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import dev.sogong_girls.mr_world.domain.souvenir.dto.SouvenirResponse;
import dev.sogong_girls.mr_world.domain.souvenir.entity.Souvenir;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SouvenirMapper {

    SouvenirResponse toResponse(Souvenir souvenir);

    List<SouvenirResponse> toResponses(List<Souvenir> souvenirs);

}
