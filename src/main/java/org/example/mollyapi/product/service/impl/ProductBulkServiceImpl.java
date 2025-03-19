package org.example.mollyapi.product.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.product.handler.ExcelHandler;
import org.example.mollyapi.product.mapper.ProductItemMapper;
import org.example.mollyapi.product.mapper.ProductMapper;
import org.example.mollyapi.product.service.ProductBulkService;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.*;


import static org.example.mollyapi.common.exception.error.impl.ProductItemError.PROBLEM_REGISTERING_BULK_PRODUCTS;
import static org.example.mollyapi.common.exception.error.impl.UserError.NOT_EXISTS_USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBulkServiceImpl implements ProductBulkService {

    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final ProductItemMapper productItemMapper;

    public List<Map<String, String>> saveChunkOfBulkProducts(MultipartFile file, Long userId) {
        List<Map<String, String>> invalidProducts = new ArrayList<>();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(NOT_EXISTS_USER));

        try (OPCPackage pkg = OPCPackage.open(file.getInputStream())) {

            ReadOnlySharedStringsTable sharedStringsTable = new ReadOnlySharedStringsTable(pkg);
            XSSFReader reader = new XSSFReader(pkg);
            XMLReader parser = XMLReaderFactory.createXMLReader();

            // 커스텀 핸들러 생성 - 여기서 invalidProducts, userId, 배치 저장 로직을 처리함
            ExcelHandler handler = new ExcelHandler(sharedStringsTable, invalidProducts,
                user.getUserId(), productMapper, productItemMapper);

            parser.setContentHandler(handler);
            Iterator<InputStream> sheets = reader.getSheetsData();

            if (sheets.hasNext()) {
                InputStream sheetStream = sheets.next();
                parser.parse(new InputSource(sheetStream));
            }

        } catch (Exception e) {
            log.error("error Message : {}", e.getMessage());
            throw new CustomException(PROBLEM_REGISTERING_BULK_PRODUCTS);
        }
        return invalidProducts;
    }
}
