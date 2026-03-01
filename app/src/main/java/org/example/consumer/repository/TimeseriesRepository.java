package org.example.consumer.repository;

import org.example.consumer.model.TimeSeriesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeseriesRepository extends JpaRepository<TimeSeriesRecord, Long> {
}
