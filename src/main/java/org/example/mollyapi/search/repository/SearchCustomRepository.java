package org.example.mollyapi.search.repository;

import org.example.mollyapi.search.dto.AutoWordResDto;
import org.example.mollyapi.search.dto.ItemDto;
import org.example.mollyapi.search.dto.SearchItemResDto;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;

public interface SearchCustomRepository {

    SearchItemResDto search(String keyword,
                            Long cursorId,
                            LocalDateTime lastCreatedAt,
                            int pageSize);

    AutoWordResDto searchAutoWord(String keyword);
}
