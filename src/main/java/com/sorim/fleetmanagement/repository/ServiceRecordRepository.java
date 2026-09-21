package com.sorim.fleetmanagement.repository;

import com.sorim.fleetmanagement.entity.ServiceRecord;
import com.sorim.fleetmanagement.entity.ServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    Page<ServiceRecord> findByStatus(ServiceStatus status, Pageable pageable);
    Page<ServiceRecord> findByVehicleId(Long vehicleId, Pageable pageable);
    Page<ServiceRecord> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT sr FROM ServiceRecord sr WHERE " +
           "(:status IS NULL OR sr.status = :status) AND " +
           "(:vehicleId IS NULL OR sr.vehicle.id = :vehicleId) AND " +
           "(:userId IS NULL OR sr.user.id = :userId)")
    Page<ServiceRecord> searchServiceRecords(@Param("status") ServiceStatus status,
                                             @Param("vehicleId") Long vehicleId,
                                             @Param("userId") Long userId,
                                             Pageable pageable);

    List<ServiceRecord> findByVehicleIdOrderByScheduledDateDesc(Long vehicleId);
}
