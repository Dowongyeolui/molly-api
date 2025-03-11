package org.example.mollyapi.product.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.client.ImageClient;
import org.example.mollyapi.common.enums.ImageType;
import org.example.mollyapi.product.dto.ProductAndThumbnailDto;
import org.example.mollyapi.product.dto.ProductDto;
import org.example.mollyapi.product.dto.ProductItemDto;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.dto.response.ColorDetailDto;
import org.example.mollyapi.product.dto.response.FileInfoDto;
import org.example.mollyapi.product.dto.response.ProductResDto;
import org.example.mollyapi.product.dto.response.SizeDetailDto;
import org.example.mollyapi.product.entity.Category;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.entity.ProductImage;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.CategoryRepository;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CategoryService categoryService;
    private final ImageClient imageClient;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductItemRepository productItemRepository;
    private final CategoryRepository categoryRepository;


    @Override
    @Transactional
    public ProductResDto updateProduct(Long userId, Long id, ProductDto productDto, List<ProductItemDto> productItemDtoList) {
        // 업데이트 할 상품 조회
        Product product = productRepository.findById(id).orElseThrow(IllegalArgumentException::new);

        User user = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);

        Product updated = product.update(
                categoryService.getCategory(productDto.categories()),
                productDto.brandName(),
                productDto.productName(),
                productDto.price(),
                productDto.description()
        );

        if (productItemDtoList != null && !productItemDtoList.isEmpty()) {
            updateProductItems(productItemDtoList);
        }

        return convertToProductResDto(updated);
    }

    private void updateProductItems(List<ProductItemDto> productItemDtoList) {
        productItemDtoList.forEach(productItemDto -> {
            ProductItem item = productItemRepository.findById(productItemDto.id()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 아이템입니다"));
            item.updateQuantity(productItemDto.quantity());
        });
    }

    @Override
    @Transactional
    public ProductResDto registerProduct(
            Long userId,
            ProductDto productDto,
            List<ProductItemDto> productItemDtoList,
            MultipartFile thumbnailImage,
            List<MultipartFile> productImages,
            List<MultipartFile> descriptionImages
    ) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 상품 엔티티 생성
        Product product = createProduct(productDto, user);

        // 이미지 리스트 생성 및 상품에 추가
        registerThumbnail(product, thumbnailImage);
        registerProductImages(product, productImages);
        regsierDescriptionImages(product, descriptionImages);

        // 상품 아이템 리스트 생성 및 상품에 추가
        registerProductItems(product, productItemDtoList);

        // 상품 저장 (연관된 이미지와 아이템도 함께 저장됨)
        Product savedProduct = productRepository.save(product);

        return convertToProductResDto(savedProduct);
    }


    private Product registerThumbnail(Product product, MultipartFile thumbnailImage) {
//        UploadFile uploadFile = fileStore.storeFile(thumbnailImage);
        UploadFile uploadFile = imageClient.upload(ImageType.PRODUCT ,thumbnailImage).orElseThrow();
        ProductImage thumbnail = ProductImage.createThumbnail(product, uploadFile);
        product.addImage(thumbnail);
        return product;
    }

    private Product registerProductImages(Product product, List<MultipartFile> productImages) {
//        List<UploadFile> uploadFiles = fileStore.storeFiles(productImages);
        List<UploadFile> uploadFiles = imageClient.upload(ImageType.PRODUCT ,productImages);
        for (int i = 0; i < uploadFiles.size(); i++) {
            UploadFile uploadFile = uploadFiles.get(i);
            ProductImage productImage = ProductImage.createProductImage(product, uploadFile, i+1);
            product.addImage(productImage);
        }
        return product;
    }

    private Product regsierDescriptionImages(Product product, List<MultipartFile> descriptionImages) {
//        List<UploadFile> uploadFiles = fileStore.storeFiles(descriptionImages);
        List<UploadFile> uploadFiles = imageClient.upload(ImageType.PRODUCT ,descriptionImages);
        for (int i = 0; i < uploadFiles.size(); i++) {
            UploadFile uploadFile = uploadFiles.get(i);
            ProductImage productImage = ProductImage.createDescriptionImage(product, uploadFile, i);
            product.addImage(productImage);
        }
        return product;
    }

    private Product registerProductItems(Product product, List<ProductItemDto> productItemDtoList) {
        for (ProductItemDto productItemDto : productItemDtoList) {
            ProductItem productItem = ProductItem.builder()
                    .color(productItemDto.color())
                    .colorCode(productItemDto.colorCode())
                    .size(productItemDto.size())
                    .quantity(productItemDto.quantity())
                    .product(product)
                    .build();
            product.addItem(productItem);
        }
        return product;
    }

    private Product createProduct(ProductDto productDto, User user) {
        // 카테고리 검색
        Category category = categoryService.getCategory(productDto.categories());

        return Product.builder()
                .category(category)
                .brandName(productDto.brandName())
                .productName(productDto.productName())
                .price(productDto.price())
                .description(productDto.description())
                .user(user)
                .build();
    }


    @Override
    @Transactional
    public void deleteProduct(Long userId,Long id) {
        Product product = productRepository.findById(id).orElseThrow(IllegalArgumentException::new);
        if (product.getUser().getUserId().equals(userId)) {
            productRepository.deleteById(id);
        }
    }

    public List<ColorDetailDto> groupItemByColor(List<ProductItem> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getColor() + "_" + item.getColorCode(), // 그룹화 키 생성
                        LinkedHashMap::new, // 순서를 유지하는 맵 사용
                        Collectors.toList()
                ))
                .values()
                .stream()
                .map(groupedItems -> new ColorDetailDto(
                        groupedItems.get(0).getColor(),
                        groupedItems.get(0).getColorCode(),
                        groupedItems.stream()
                                .map(item -> new SizeDetailDto(item.getId(), item.getSize(), item.getQuantity()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    public ProductResDto convertToProductResDto(Product product) {
        FileInfoDto thumbnail = new FileInfoDto(product.getThumbnail().getStoredFileName(), product.getThumbnail().getUploadFileName());
        List<FileInfoDto> productImages = product.getProductImages().stream().map((item)-> new FileInfoDto(item.getStoredFileName(), item.getUploadFileName())).toList();
        List<FileInfoDto> descriptionImages = product.getDescriptionImages().stream().map(item -> new FileInfoDto(item.getStoredFileName(), item.getUploadFileName())).toList();

        List<ProductItemDto> itemResDtos = product.getItems().stream().map(ProductItemDto::from).toList();
        List<ColorDetailDto> colorDetails = groupItemByColor(product.getItems());

        return new ProductResDto(
                product.getId(),
                categoryService.getCategoryPath(product.getCategory()),
                product.getBrandName(),
                product.getProductName(),
                product.getPrice(),
                product.getDescription(),
                thumbnail,
                productImages,
                descriptionImages,
                itemResDtos,
                colorDetails
        );
    }

    public ProductResDto convertToProductResDto(ProductAndThumbnailDto productAndThumbnailDto) {
        FileInfoDto thumbnail = new FileInfoDto(
                productAndThumbnailDto.getUrl(), productAndThumbnailDto.getFilename());

        Category category = categoryRepository.findById(productAndThumbnailDto.getCategoryId()).orElseThrow();
        return new ProductResDto(
                productAndThumbnailDto.getId(),
                categoryService.getCategoryPath(category),
                productAndThumbnailDto.getBrandName(),
                productAndThumbnailDto.getProductName(),
                productAndThumbnailDto.getPrice(),
                productAndThumbnailDto.getDescription(),
                thumbnail,
                null,
                null,
                null,
                null
        );
    }
}
