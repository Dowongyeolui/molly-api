package org.example.mollyapi.search.service;


import lombok.RequiredArgsConstructor;
import org.example.mollyapi.search.dto.SearchCommonResDto;
import org.example.mollyapi.search.dto.SearchItemResDto;
import org.example.mollyapi.search.dto.SearchOptionReqDto;
import org.example.mollyapi.search.entity.Search;
import org.example.mollyapi.search.repository.SearchRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;

    public SearchItemResDto searchItem(SearchOptionReqDto searchOptionReqDto){

        int pageSize = 48;
        if( searchOptionReqDto.pageSize() != null) {
            pageSize = searchOptionReqDto.pageSize();
        }

        Search search = searchRepository.findByKeyword(searchOptionReqDto.keyword())
                .orElse(
                        Search.builder()
                                .keyword(searchOptionReqDto.keyword())
                                .count(0)
                                .build()
                );

        search.increaseCount();
        searchRepository.save(search);

        return searchRepository.search(searchOptionReqDto, pageSize);
    }

    public SearchCommonResDto searchWord(String keyword) {

        return searchRepository.searchAutoWord(keyword);
    }

    public SearchCommonResDto searchBrand(String keyword) {

        return searchRepository.searchBrand(keyword);
    }
}
