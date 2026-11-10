package dev.sogong_girls.mr_world.domain.tour.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import dev.sogong_girls.mr_world.domain.tour.dto.TourUpdateRequest;
import dev.sogong_girls.mr_world.domain.tour.entity.FoodOption;
import dev.sogong_girls.mr_world.domain.tour.entity.HotelOption;
import dev.sogong_girls.mr_world.domain.tour.entity.Tour;
import dev.sogong_girls.mr_world.domain.tour.entity.TourStyle;
import dev.sogong_girls.mr_world.domain.tour.enums.FoodType;
import dev.sogong_girls.mr_world.domain.tour.enums.HotelGrade;
import dev.sogong_girls.mr_world.domain.tour.enums.TourStyleType;
import dev.sogong_girls.mr_world.domain.tour.enums.TourTheme;
import dev.sogong_girls.mr_world.domain.tour.enums.TransportType;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
class AdminTourServiceTest {
    @Autowired
    AdminTourService service;
    @Autowired
    EntityManager entityManager;
    @Autowired
    PlatformTransactionManager transactionManager;

    private TransactionTemplate transaction;
    private final List<Long> tourIds = new ArrayList<>();
    private Tour tour;
    private TourStyle style;
    private HotelOption hotel;
    private FoodOption food;

    @BeforeEach
    void setUp() {
        transaction = new TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            tour = newTour();
            hotel = newHotel(tour.getId(), HotelGrade._4_STAR);
            food = newFood(tour.getId(), FoodType.LOCAL_RESTAURANT);
            style = newStyle(TourStyleType.GRAND, hotel.getId(), food.getId());
        });
    }

    @AfterEach
    void cleanUp() {
        transaction.executeWithoutResult(status -> {
            for (Long id : tourIds) {
                for (String entity : List.of("TourStyle", "HotelOption", "FoodOption")) {
                    entityManager.createQuery("delete from " + entity + " where tourId = :id")
                            .setParameter("id", id).executeUpdate();
                }
                entityManager.createQuery("delete from Tour where id = :id")
                        .setParameter("id", id).executeUpdate();
            }
        });
    }

    @Test
    void replacesHotelAndFoodWithMatchingOptionsFromSameTour() {
        HotelOption replacementHotel = transaction.execute(status -> newHotel(tour.getId(), HotelGrade._4_STAR));
        FoodOption replacementFood = transaction.execute(status -> newFood(tour.getId(), FoodType.LOCAL_RESTAURANT));

        service.updateTour(tour.getId(), request(null, styleRequest(null,
                hotelRequest(replacementHotel.getId(), null), foodRequest(replacementFood.getId(), null))));

        transaction.executeWithoutResult(status -> {
            TourStyle saved = entityManager.find(TourStyle.class, style.getId());
            assertThat(saved.getHotelOptionId()).isEqualTo(replacementHotel.getId());
            assertThat(saved.getFoodOptionId()).isEqualTo(replacementFood.getId());
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void rejectsReplacementOptionWithWrongGrade(boolean replaceHotel) {
        HotelOption replacementHotel = transaction.execute(status -> newHotel(tour.getId(), HotelGrade._5_STAR));
        FoodOption replacementFood = transaction.execute(status -> newFood(tour.getId(), FoodType.PREMIUM_RESTAURANT));

        assertInvalid(request(null, styleRequest(null,
                replaceHotel ? hotelRequest(replacementHotel.getId(), null) : null,
                replaceHotel ? null : foodRequest(replacementFood.getId(), null))));

        assertOriginalOptions();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void rollsBackTourAndOptionChangesWhenGradeDoesNotMatch(boolean changeHotel) {
        assertInvalid(request(null, styleRequest(null,
                changeHotel ? hotelRequest(hotel.getId(), HotelGrade._5_STAR) : null,
                changeHotel ? null : foodRequest(food.getId(), FoodType.PREMIUM_RESTAURANT))));

        transaction.executeWithoutResult(status -> {
            assertThat(entityManager.find(Tour.class, tour.getId()).getName()).isEqualTo("Original tour");
            assertThat(entityManager.find(HotelOption.class, hotel.getId()).getGrade()).isEqualTo(HotelGrade._4_STAR);
            assertThat(entityManager.find(FoodOption.class, food.getId()).getType()).isEqualTo(FoodType.LOCAL_RESTAURANT);
        });
    }

    @Test
    void updatesStyleAndOptionGradesTogether() {
        service.updateTour(tour.getId(), request(null, styleRequest(TourStyleType.PREMIUM,
                hotelRequest(hotel.getId(), HotelGrade._5_STAR),
                foodRequest(food.getId(), FoodType.PREMIUM_RESTAURANT))));

        transaction.executeWithoutResult(status -> {
            assertThat(entityManager.find(TourStyle.class, style.getId()).getType()).isEqualTo(TourStyleType.PREMIUM);
            assertThat(entityManager.find(HotelOption.class, hotel.getId()).getGrade()).isEqualTo(HotelGrade._5_STAR);
            assertThat(entityManager.find(FoodOption.class, food.getId()).getType()).isEqualTo(FoodType.PREMIUM_RESTAURANT);
        });
    }

    @Test
    void checksRetainedOptionsWhenOnlyStyleTypeChanges() {
        assertInvalid(request(null, styleRequest(TourStyleType.PREMIUM, null, null)));
    }

    @Test
    void rejectsDuplicateTypeAgainstStyleOmittedFromRequestBeforeDatabaseFlush() {
        transaction.executeWithoutResult(status -> newStyle(TourStyleType.PREMIUM, null, null));

        assertThatThrownBy(() -> service.updateTour(tour.getId(), request(null,
                styleRequest(TourStyleType.PREMIUM, hotelRequest(hotel.getId(), HotelGrade._5_STAR),
                        foodRequest(food.getId(), FoodType.PREMIUM_RESTAURANT)))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("투어 스타일은 중복될 수 없습니다.");

        transaction.executeWithoutResult(status ->
                assertThat(entityManager.find(TourStyle.class, style.getId()).getType()).isEqualTo(TourStyleType.GRAND));
    }

    @Test
    void checksExistingStylesWhenOnlyThemeChanges() {
        transaction.executeWithoutResult(status -> newStyle(TourStyleType.CLASSIC, null, null));

        assertInvalid(request(TourTheme.HONEYMOON));

        transaction.executeWithoutResult(status ->
                assertThat(entityManager.find(Tour.class, tour.getId()).getTourTheme()).isEqualTo(TourTheme.TREKKING));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void rejectsOptionsBelongingToAnotherTour(boolean replaceHotel) {
        Tour other = transaction.execute(status -> newTour());
        HotelOption otherHotel = transaction.execute(status -> newHotel(other.getId(), HotelGrade._4_STAR));
        FoodOption otherFood = transaction.execute(status -> newFood(other.getId(), FoodType.LOCAL_RESTAURANT));

        assertInvalid(request(null, styleRequest(null,
                replaceHotel ? hotelRequest(otherHotel.getId(), null) : null,
                replaceHotel ? null : foodRequest(otherFood.getId(), null))));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void checksOtherStylesAffectedBySharedOptionGradeChange(boolean changeHotel) {
        HotelOption premiumHotel = transaction.execute(status -> newHotel(tour.getId(), HotelGrade._5_STAR));
        FoodOption premiumFood = transaction.execute(status -> newFood(tour.getId(), FoodType.PREMIUM_RESTAURANT));
        transaction.executeWithoutResult(status -> newStyle(TourStyleType.PREMIUM,
                premiumHotel.getId(), premiumFood.getId()));

        assertInvalid(request(null, styleRequest(null,
                changeHotel ? hotelRequest(premiumHotel.getId(), HotelGrade._4_STAR) : null,
                changeHotel ? null : foodRequest(premiumFood.getId(), FoodType.LOCAL_RESTAURANT))));

        transaction.executeWithoutResult(status -> {
            assertThat(entityManager.find(HotelOption.class, premiumHotel.getId()).getGrade()).isEqualTo(HotelGrade._5_STAR);
            assertThat(entityManager.find(FoodOption.class, premiumFood.getId()).getType()).isEqualTo(FoodType.PREMIUM_RESTAURANT);
        });
    }

    private TourUpdateRequest request(TourTheme theme, TourUpdateRequest.TourStyleRequest... styles) {
        return new TourUpdateRequest("Changed tour", null, null, theme, null, null, null, null,
                null, null, null, null, styles.length == 0 ? null : List.of(styles), null);
    }

    private TourUpdateRequest.TourStyleRequest styleRequest(TourStyleType type,
            TourUpdateRequest.HotelOptionRequest hotelRequest, TourUpdateRequest.FoodOptionRequest foodRequest) {
        return new TourUpdateRequest.TourStyleRequest(style.getId(), type, null, null, hotelRequest, foodRequest, null);
    }

    private TourUpdateRequest.HotelOptionRequest hotelRequest(Long id, HotelGrade grade) {
        return new TourUpdateRequest.HotelOptionRequest(id, null, null, grade, null, null);
    }

    private TourUpdateRequest.FoodOptionRequest foodRequest(Long id, FoodType type) {
        return new TourUpdateRequest.FoodOptionRequest(id, null, type, null, null);
    }

    private Tour newTour() {
        Tour created = persist(Tour.builder().name("Original tour").departure("Seoul").destination("Busan")
                .tourTheme(TourTheme.TREKKING).transportType(TransportType.PREMIUM_CAR).build());
        tourIds.add(created.getId());
        return created;
    }

    private HotelOption newHotel(Long tourId, HotelGrade grade) {
        return persist(HotelOption.builder().tourId(tourId).name("Hotel").location("Busan")
                .grade(grade).price(100000).build());
    }

    private FoodOption newFood(Long tourId, FoodType type) {
        return persist(FoodOption.builder().tourId(tourId).name("Food").type(type).price(10000).build());
    }

    private TourStyle newStyle(TourStyleType type, Long hotelId, Long foodId) {
        return persist(TourStyle.builder().tourId(tour.getId()).type(type).basePrice(100000)
                .hotelOptionId(hotelId).foodOptionId(foodId).build());
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    private void assertInvalid(TourUpdateRequest request) {
        assertThatThrownBy(() -> service.updateTour(tour.getId(), request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(BusinessErrorCode.INVALID_INPUT));
    }

    private void assertOriginalOptions() {
        transaction.executeWithoutResult(status -> {
            TourStyle saved = entityManager.find(TourStyle.class, style.getId());
            assertThat(saved.getHotelOptionId()).isEqualTo(hotel.getId());
            assertThat(saved.getFoodOptionId()).isEqualTo(food.getId());
        });
    }
}
