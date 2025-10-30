package com.example.blog.repository;

import com.example.blog.domain.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * SiteSetting JPA Repository
 */
public interface SiteSettingRepository extends JpaRepository<SiteSetting, Long> {
    Optional<SiteSetting> findByKey(String key);
}
