package org.example.socialmedia_services.repository.jointTrip;

import org.example.socialmedia_services.entity.jointTrip.TripDayByDayItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripDayByDayItineraryRepository extends JpaRepository<TripDayByDayItinerary, Long> {

    // Basic queries
    @Query("SELECT td FROM TripDayByDayItinerary td WHERE td.jointTrip.tripId = :tripId AND td.isActive = true ORDER BY td.dayNo ASC")
    List<TripDayByDayItinerary> findByTripIdOrderByDayNo(@Param("tripId") Long tripId);

    @Query("SELECT td FROM TripDayByDayItinerary td WHERE td.jointTrip.tripId = :tripId AND td.dayNo = :dayNo AND td.isActive = true")
    Optional<TripDayByDayItinerary> findByTripIdAndDayNo(@Param("tripId") Long tripId, @Param("dayNo") Integer dayNo);

    @Query("SELECT td FROM TripDayByDayItinerary td WHERE td.itineraryId = :itineraryId AND td.isActive = true")
    Optional<TripDayByDayItinerary> findActiveById(@Param("itineraryId") Long itineraryId);

    // Search queries for places, accommodations, restaurants
    @Query("SELECT DISTINCT td FROM TripDayByDayItinerary td JOIN td.visitingPlaces vp WHERE LOWER(vp) LIKE LOWER(CONCAT('%', :place, '%')) AND td.isActive = true")
    List<TripDayByDayItinerary> findByVisitingPlace(@Param("place") String place);

    @Query("SELECT DISTINCT td FROM TripDayByDayItinerary td JOIN td.accommodations acc WHERE LOWER(acc) LIKE LOWER(CONCAT('%', :accommodation, '%')) AND td.isActive = true")
    List<TripDayByDayItinerary> findByAccommodation(@Param("accommodation") String accommodation);

    @Query("SELECT DISTINCT td FROM TripDayByDayItinerary td JOIN td.restaurants rest WHERE LOWER(rest) LIKE LOWER(CONCAT('%', :restaurant, '%')) AND td.isActive = true")
    List<TripDayByDayItinerary> findByRestaurant(@Param("restaurant") String restaurant);

    // Statistics queries
    @Query("SELECT vp, COUNT(vp) FROM TripDayByDayItinerary td JOIN td.visitingPlaces vp WHERE td.isActive = true GROUP BY vp ORDER BY COUNT(vp) DESC")
    List<Object[]> getMostPopularVisitingPlaces();

    @Query("SELECT acc, COUNT(acc) FROM TripDayByDayItinerary td JOIN td.accommodations acc WHERE td.isActive = true GROUP BY acc ORDER BY COUNT(acc) DESC")
    List<Object[]> getMostPopularAccommodations();

    @Query("SELECT rest, COUNT(rest) FROM TripDayByDayItinerary td JOIN td.restaurants rest WHERE td.isActive = true GROUP BY rest ORDER BY COUNT(rest) DESC")
    List<Object[]> getMostPopularRestaurants();

    // Update queries
    @Modifying
    @Query("UPDATE TripDayByDayItinerary td SET td.isActive = false, td.updatedAt = CURRENT_TIMESTAMP WHERE td.itineraryId = :itineraryId")
    int deactivateItinerary(@Param("itineraryId") Long itineraryId);

    @Modifying
    @Query("UPDATE TripDayByDayItinerary td SET td.isActive = false, td.updatedAt = CURRENT_TIMESTAMP WHERE td.jointTrip.tripId = :tripId")
    int deactivateAllItinerariesForTrip(@Param("tripId") Long tripId);

    @Modifying
    @Query("UPDATE TripDayByDayItinerary td SET td.otherDetails = :details, td.updatedAt = CURRENT_TIMESTAMP WHERE td.itineraryId = :itineraryId")
    int updateOtherDetails(@Param("itineraryId") Long itineraryId, @Param("details") String details);

    // Count queries
    @Query("SELECT COUNT(td) FROM TripDayByDayItinerary td WHERE td.jointTrip.tripId = :tripId AND td.isActive = true")
    long countActiveItinerariesForTrip(@Param("tripId") Long tripId);

    @Query("SELECT MAX(td.dayNo) FROM TripDayByDayItinerary td WHERE td.jointTrip.tripId = :tripId AND td.isActive = true")
    Integer getMaxDayNoForTrip(@Param("tripId") Long tripId);
}