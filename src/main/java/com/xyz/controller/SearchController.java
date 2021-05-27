package com.xyz.controller;

import com.xyz.model.SearchResult;
import com.xyz.service.SearchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j(topic = "xyz-indexing-service")
@AllArgsConstructor
@RestController
@RequestMapping("/api/V1/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/{term}")
    public @ResponseBody List<SearchResult> search(@PathVariable String term) {
        log.info("Searching for files with the term {} ...", term);
        return searchService.search(term);
    }

    @GetMapping("/{term}/fuzzy")
    public @ResponseBody List<SearchResult> searchFuzzy(@PathVariable String term) {
        log.info("Searching for files with the fuzzy term {} ...", term);
        return searchService.searchFuzzy(term);
    }

}
