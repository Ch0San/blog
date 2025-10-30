package com.example.blog.service;

import com.example.blog.domain.SiteSetting;
import com.example.blog.repository.SiteSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사이트 설정 서비스
 */
@Service
public class SiteSettingService {
    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    // 설정 값 조회
    public String getSetting(String key, String defaultValue) {
        return siteSettingRepository.findByKey(key)
                .map(SiteSetting::getValue)
                .orElse(defaultValue);
    }

    // 설정 값 저장/업데이트
    @Transactional
    public void saveSetting(String key, String value, String description) {
        SiteSetting setting = siteSettingRepository.findByKey(key)
                .orElse(new SiteSetting());

        setting.setKey(key);
        setting.setValue(value);
        setting.setDescription(description);

        siteSettingRepository.save(setting);
    }
}
