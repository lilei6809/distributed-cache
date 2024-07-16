package com.lanlan.cache.controller;

import com.lanlan.cache.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    @Autowired
    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> put(@PathVariable String key, @RequestBody String value) {
        cacheService.put(key, value);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> get(@PathVariable String key) {
        return cacheService.get(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> remove(@PathVariable String key) {
        cacheService.remove(key);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<CacheStats> getStats() {
        CacheStats stats = new CacheStats(cacheService.getSize(), cacheService.getEvictionCount());
        return ResponseEntity.ok(stats);
    }

    static class CacheStats {
        public final int size;
        public final int evictionCount;

        public CacheStats(int size, int evictionCount) {
            this.size = size;
            this.evictionCount = evictionCount;
        }
    }
}
