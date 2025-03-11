package org.example.mollyapi.product.service;

import org.example.mollyapi.product.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findByName(String name);
    List<String> getCategoryPath(Category category);
    List<String> getCategoryPath(Long id);
    Category getCategory(List<String> categories);
    List<Category> getLeafCategories(Category category);
    List<Category> getAllLeafCategories(List<Category> categoryList);
    List<Category> findEndWith(List<String> categories);
    List<Category> findEndWith(String categories);
}
