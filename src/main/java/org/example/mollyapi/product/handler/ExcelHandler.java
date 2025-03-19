package org.example.mollyapi.product.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.example.mollyapi.product.dto.request.ProductBulkItemReqDto;
import org.example.mollyapi.product.dto.request.ProductBulkReqDto;
import org.example.mollyapi.product.mapper.ProductItemMapper;
import org.example.mollyapi.product.mapper.ProductMapper;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.example.mollyapi.product.dto.request.ProductBulkItemReqDto.createBulkProductItemReqDto;

@Slf4j
public class ExcelHandler extends DefaultHandler {


    private final ProductMapper productMapper;
    private final ProductItemMapper productItemMapper;
    private final ReadOnlySharedStringsTable sharedStringsTable;
    private final Long userId;

    // 등록에 실패한 상품
    private final List<Map<String, String>> invalidProducts;

    private static final int BATCH_SIZE = 10000;
    private static final Pattern HEX_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6})$");

    // 배치로 저장할 DTO 리스트
    private final List<ProductBulkReqDto> passedProduct = new ArrayList<>();
    private final List<ProductBulkItemReqDto> passedProductItem = new ArrayList<>();

    // 현재 행의 데이터 저장 (각 셀의 문자열 값)
    private final List<String> currentRow = new ArrayList<>();
    private final List<List<Long>> productItemIds = new ArrayList<>();
    private final StringBuilder cellValueBuilder = new StringBuilder();
    private int rowIndex = 0; // 전체 행 번호 (헤더 포함)
    private boolean inValue = false;
    private boolean isSharedString = false;


    public ExcelHandler(ReadOnlySharedStringsTable sharedStringsTable,
        List<Map<String, String>> invalidProducts, Long userId, ProductMapper productMapper,
        ProductItemMapper productItemMapper) {
        this.sharedStringsTable = sharedStringsTable;
        this.invalidProducts = invalidProducts;
        this.userId = userId;
        this.productMapper = productMapper;
        this.productItemMapper = productItemMapper;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        isRowStart(qName);
        checkCellType(qName, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (inValue) {
            cellValueBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        // 셀 값 종료 시
        if ("c".equals(qName)) {  // <v> 태그 종료 시 값 저장
            inValue = false;
            String rawValue = cellValueBuilder.toString().trim();
            String cellValue;

            if (isSharedString) {
                int idx = Integer.parseInt(rawValue);
                cellValue = sharedStringsTable.getItemAt(idx).getString();
            } else {
                cellValue = rawValue;
            }

            currentRow.add(cellValue);  // 변환된 값 추가
        }

        // (<row> 태그 종료)
        if ("row".equals(qName)) {

            // 헤더 행이라면 건너뜁니다.
            if (rowIndex == 1) {
                return;
            }
            // 컬럼 수가 부족하면 무시
            if (currentRow.size() < 9) {
                return;
            }

            String productName = currentRow.get(0);
            String description = currentRow.get(1);
            long categoryId = Long.parseLong(currentRow.get(2));
            String brandName = currentRow.get(3);
            long price = Long.parseLong(currentRow.get(4));
            String color = currentRow.get(5);
            String colorCode = currentRow.get(6);
            long quantity = Long.parseLong(currentRow.get(7));
            String size = currentRow.get(8);

            Map<String, String> errorMap = validProduct(productName, description, quantity,
                brandName, color, colorCode, price, size, rowIndex);

            if (errorMap.isEmpty()) {
                invalidProducts.add(errorMap);
                return;
            }

            //엑셀 데이터 DTO로 변환
            excelToProductBulkDto(productName, color, colorCode, quantity, size, description,
                categoryId, brandName, price);

            // 배치 크기에 도달하면 DB에 저장 후 리스트 초기화
            if (passedProductItem.size() >= BATCH_SIZE) {
                saveProductByMybatis(userId, passedProduct, passedProductItem);
                checkedFailSaveProduct();

                passedProduct.clear();
                passedProductItem.clear();
            }


        }
    }

    /**
     * excel Data Dto 로 변환
     *
     * @param productName 상품 명
     * @param color       상품 색
     * @param colorCode   상품 색 코드
     * @param quantity    재고
     * @param size        사이즈
     * @param description 설명
     * @param categoryId  카테고리
     * @param brandName   브랜드 이름
     * @param price       가격
     */
    private void excelToProductBulkDto(String productName, String color, String colorCode,
        long quantity,
        String size, String description, long categoryId, String brandName, long price) {
        boolean existProduct = false;
        ProductBulkItemReqDto productBulkItemReqDto = null;

        for (ProductBulkReqDto dto : passedProduct) {
            if (dto.getProductName().equals(productName)) {
                productBulkItemReqDto = createBulkProductItemReqDto(dto.getId(), color, colorCode,
                    quantity, size);
                existProduct = true;
                break;
            }
        }

        if (!existProduct) {
            ProductBulkReqDto productBulkReqDto = ProductBulkReqDto.builder()
                .productName(productName)
                .description(description)
                .categoryId(categoryId)
                .brandName(brandName)
                .price(price)
                .build();
            productBulkItemReqDto = createBulkProductItemReqDto(productBulkReqDto.getId(), color,
                colorCode, quantity, size);
            passedProduct.add(productBulkReqDto);
        }

        productItemIds.add(List.of(productBulkItemReqDto.getItemId(), (long) rowIndex));
        passedProductItem.add(productBulkItemReqDto);
    }


    @Override
    public void endDocument() {
        // 마지막 남은 데이터 저장
        if (!passedProductItem.isEmpty()) {
            saveProductByMybatis(userId, passedProduct, passedProductItem);
            checkedFailSaveProduct();

            passedProduct.clear();
            passedProductItem.clear();

        }
    }

    /**
     * DB의 누락된 상품을 탐색 후, 등록하지 못한 상품 목록에 추가
     */
    private void checkedFailSaveProduct() {
        List<Long> savedProductIds = productItemMapper.getProductsByIdRange(
            productItemIds.get(0).get(0),
            productItemIds.get(productItemIds.size() - 1).get(0));

        int savedProductIndex = 0;
        int productItemIndex = 0;
        while (productItemIndex < productItemIds.size()) {

            long savedProductId = savedProductIds.get(savedProductIndex);
            List<Long> productItem = productItemIds.get(productItemIndex);
            Long productId = productItem.get(0);

            while (savedProductId != productId) {

                Map<String, String> errorMap = new HashMap<>();

                errorMap.put("행", String.valueOf(productItem.get(1)));
                invalidProducts.add(errorMap);

                if (savedProductId > productId) {
                    productItemIndex++;
                    productItem = productItemIds.get(productItemIndex);
                    productId = productItem.get(0);
                }
            }
            productItemIndex++;
            savedProductIndex++;
        }
    }

    /**
     * 엑셀 데이터 유효성 검사
     *
     * @param productName 상품명
     * @param description 상품 설명
     * @param quantity    상품 재고
     * @param brandName   상품 브랜드
     * @param color       색
     * @param colorCode   색 코드
     * @param price       가격
     * @param size        사이즈
     * @param rowIndex    엑셀 ROW
     * @return
     */
    private Map<String, String> validProduct(String productName, String description, long quantity,
        String brandName, String color, String colorCode, long price, String size, int rowIndex) {
        Map<String, String> rowErrorMap = new HashMap<>();

        if (productName == null || productName.isBlank()) {
            rowErrorMap.put("상품명", "유효하지 않은 상품명");
        }

        if (description == null || description.length() < 10) {
            rowErrorMap.put("상품설명", "설명은 최소 10글자 이상");
        }

        if (quantity <= 0) {
            rowErrorMap.put("수량", "수량은 1 이상이어야 함");
        }

        if (brandName == null || brandName.isBlank()) {
            rowErrorMap.put("브랜드명", "유효하지 않은 브랜드명");
        }

        if (color == null || color.isBlank()) {
            rowErrorMap.put("색상", "유효하지 않은 색입니다.");
        }

        if (colorCode == null || colorCode.isBlank() || !HEX_PATTERN.matcher(colorCode).matches()) {
            rowErrorMap.put("색상코드", "유효하지 않은 색 코드입니다.");
        }

        if (size == null || size.isBlank()) {
            rowErrorMap.put("사이즈", "유효하지 않은 사이즈 입니다.");
        }

        if (price < 0) {
            rowErrorMap.put("가격", "가격이 0 이상이어야 합니다.");
        }

        if (!rowErrorMap.isEmpty()) {
            rowErrorMap.put("행", String.valueOf(rowIndex));
        }
        return rowErrorMap;
    }

    /**
     * 유효성이 검사된 데이터에 한해 DB에 저장
     *
     * @param userId            상품을 등록하려는 사용자
     * @param passedProduct     유효성이 검사된 상품 데이터
     * @param passedProductItem 유효성이 검사된 상품 옵션 데이터
     */
    // 실제 DTO 생성 로직으로 대체
    private void saveProductByMybatis(Long userId, List<ProductBulkReqDto> passedProduct,
        List<ProductBulkItemReqDto> passedProductItem) {
        LocalDateTime now = LocalDateTime.now();

        productMapper.insertProducts(passedProduct, userId, now);
        productItemMapper.insertProductItems(passedProductItem, now);
    }


    /**
     * 셀을 탐색하다가 새로운 헹인지 검사
     *
     * @param qName 셀 이름
     */
    private void isRowStart(String qName) {
        if ("row".equals(qName)) {
            currentRow.clear();
            rowIndex++;
        }
    }

    /**
     * 엑셀의 CellType 숫자, 문자인 판별
     *
     * @param qName      태그 네임
     * @param attributes 셀 속성
     */
    private void checkCellType(String qName, Attributes attributes) {
        if ("c".equals(qName)) {
            inValue = true;
            cellValueBuilder.setLength(0);

            // "t" 속성을 확인하여 데이터 타입 판별
            String cellType = attributes.getValue("t");

            if ("s".equals(cellType)) {
                // "s"는 문자열(String) -> sharedStrings.xml을 참조해야 함
                isSharedString = true;
            } else {
                // 숫자형(Number) 또는 기본 값
                isSharedString = false;
            }
        }
    }
}

