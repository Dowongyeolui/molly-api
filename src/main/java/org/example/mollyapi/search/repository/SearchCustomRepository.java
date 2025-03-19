package org.example.mollyapi.search.repository;

import org.example.mollyapi.search.dto.SearchCommonResDto;
import org.example.mollyapi.search.dto.SearchItemResDto;
import org.example.mollyapi.search.dto.SearchOptionReqDto;

public interface SearchCustomRepository {

    SearchItemResDto search(SearchOptionReqDto searchOptionReqDto, int pageSize);

    SearchCommonResDto searchAutoWord(String keyword);

    SearchCommonResDto searchBrand(String keyword);
}
