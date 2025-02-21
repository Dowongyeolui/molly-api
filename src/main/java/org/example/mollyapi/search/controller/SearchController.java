package org.example.mollyapi.search.controller;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.search.dto.SearchCommonResDto;
import org.example.mollyapi.search.dto.SearchItemResDto;
import org.example.mollyapi.search.service.SearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Search Controller", description = "검색 관련 엔드 포인트")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "검색어 기반 상품 조회 기능", description = "검색어 기반 상품 검색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = SearchItemResDto.class))),
    })
    public ResponseEntity<SearchItemResDto> search(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Long cursorId,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime lastCreatedAt,
                                                @RequestParam(required = false) Integer size) {

        int pageSize = 48;
        if( size != null) {
            pageSize = size;
        }
        SearchItemResDto searchItemResDto = searchService.searchItem(keyword, cursorId, lastCreatedAt, pageSize);

        return ResponseEntity.ok(searchItemResDto);
    }

    @GetMapping("/auto")
    @Operation(summary = "검색어 자동 완성 기능", description = "검색어 자동 완성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = SearchCommonResDto.class))),
    })
    public ResponseEntity<SearchCommonResDto> searchWord(@RequestParam(required = false) String keyword){
        SearchCommonResDto result = searchService.searchWord(keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/brand")
    @Operation(summary = "브랜드 검색 기능", description = "브랜드 검색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = SearchCommonResDto.class))),
    })
    public ResponseEntity<SearchCommonResDto> searchBrand(@RequestParam(required = false) String keyword){
        SearchCommonResDto result = searchService.searchBrand(keyword);
        return ResponseEntity.ok(result);
    }
}
