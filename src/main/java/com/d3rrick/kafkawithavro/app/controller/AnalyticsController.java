package com.d3rrick.kafkawithavro.app.controller;

import com.d3rrick.kafkawithavro.app.processor.InteractiveQueryService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final InteractiveQueryService queryService;

    @GetMapping("/domains/{domain}")
    public ResponseEntity<?> getDomainCount(@PathVariable String domain) {
        try {
            var count = queryService.getDomainCount(domain);
            return ResponseEntity.ok(count != null ? count : 0L);
        } catch (InvalidStateStoreException e) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse(e.getMessage(), 503));
        }
    }

    @GetMapping("/domains/window/{domain}")
    public ResponseEntity<?> getDomainCountWindow(@PathVariable String domain) {
        try {
            var count = queryService.getDomainVelocity(domain);
            return ResponseEntity.ok(count != null ? count : 0L);
        } catch (InvalidStateStoreException e) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse(e.getMessage(), 503));
        }
    }

    record ErrorResponse(String message, int code){}
}