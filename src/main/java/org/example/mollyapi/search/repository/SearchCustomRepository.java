package org.example.mollyapi.search.repository;

import org.example.mollyapi.search.dto.SearchCommonResDto;
import org.example.mollyapi.search.dto.SearchItemResDto;


import java.time.LocalDateTime;

public interface SearchCustomRepository {

    SearchItemResDto search(String keyword,
                            Long cursorId,
                            LocalDateTime lastCreatedAt,
                            int pageSize);

    SearchCommonResDto searchAutoWord(String keyword);

    SearchCommonResDto searchBrand(String keyword);
}
