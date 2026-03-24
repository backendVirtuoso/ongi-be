package com.ongi.domain.quote.repository;

import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Page<Quote> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    @Query(value = "SELECT * FROM tb_quote WHERE category = :category AND is_active = true ORDER BY RAND() LIMIT 1",
            nativeQuery = true)
    Optional<Quote> findRandomByCategory(@Param("category") String category);

    @Query(value = "SELECT * FROM tb_quote WHERE is_active = true ORDER BY RAND() LIMIT 1",
            nativeQuery = true)
    Optional<Quote> findRandom();
}
