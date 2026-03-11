package org.cotc.service;

import org.cotc.model.TimeSeriesRecord;
import org.cotc.utils.Translators;
import org.cotc.libcotc.dto.TimeSeriesRecordDTO;
import org.cotc.repository.ProducerRepository;
import org.cotc.repository.GroupRepository;
import org.cotc.repository.TimeseriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TimeSeriesRecordService {

    private static final Logger logger = LoggerFactory.getLogger(TimeSeriesRecordService.class);

    private final TimeseriesRepository timeseriesRepository;

    @Autowired
    public TimeSeriesRecordService(
            GroupRepository groupRepository,
            ProducerRepository producerRepository,
            TimeseriesRepository timeseriesRepository,
            Translators translators
    ) {
        this.timeseriesRepository = timeseriesRepository;
    }

    public List<TimeSeriesRecordDTO> getRecordsByGroupId(String groupName) {
        logger.debug("getRecordsByStreamId - querying for streamId={}", groupName);
        List<TimeSeriesRecordDTO> records = timeseriesRepository.findByProducerGroupName(groupName).stream()
                .map(TimeSeriesRecord::toDTO)
                .toList();
        logger.debug("getRecordsByStreamId - returning {} record(s) for streamId={}", records.size(), groupName);
        return records;
    }

}
