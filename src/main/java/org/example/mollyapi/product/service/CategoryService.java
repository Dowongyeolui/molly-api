package org.example.mollyapi.product.service;

import org.example.mollyapi.product.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findByName(String name);
    List<String> getCategoryPath(Category category);
    Category getCategory(List<String> categories);
    List<Category> getLeafCategories(Category category);
}
