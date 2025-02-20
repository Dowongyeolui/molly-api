package org.example.mollyapi.search.repository;

import org.example.mollyapi.search.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchRepository extends JpaRepository<Search, Long>, SearchCustomRepository {

    Optional<Search> findByKeyword(String keyword);
}
