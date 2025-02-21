package org.example.mollyapi.search.service;


import lombok.RequiredArgsConstructor;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.search.dto.SearchCommonResDto;
import org.example.mollyapi.search.dto.SearchItemResDto;
import org.example.mollyapi.search.entity.Search;
import org.example.mollyapi.search.repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;

    public SearchItemResDto searchItem(String keyword,
                                    Long cursorId,
                                    LocalDateTime lastCreatedAt,
                                    int pageSize){

        Search search = searchRepository.findByKeyword(keyword)
                .orElse(
                        Search.builder()
                                .keyword(keyword)
                                .count(0)
                                .build()
                );

        search.increaseCount();
        searchRepository.save(search);

        return searchRepository.search(keyword, cursorId, lastCreatedAt, pageSize);
    }

    public SearchCommonResDto searchWord(String keyword) {

        return searchRepository.searchAutoWord(keyword);
    }

    public SearchCommonResDto searchBrand(String keyword) {

        return searchRepository.searchBrand(keyword);
    }
}
