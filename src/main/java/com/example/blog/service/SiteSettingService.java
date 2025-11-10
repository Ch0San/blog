package com.example.blog.service;

import com.example.blog.domain.SiteSetting;
import com.example.blog.repository.SiteSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사이트 설정 서비스
 */
/**
 * 사이트 설정 서비스.
 *
 * 키-값 형태의 설정을 조회/저장합니다.
 */
@Service
public class SiteSettingService {
    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    // 설정 값 조회
    /**
     * 설정값을 조회합니다.
     *
     * @param key 설정 키
     * @param defaultValue 키가 없을 때 반환할 기본값
     * @return 설정 값 또는 기본값
     */
    public String getSetting(String key, String defaultValue) {
        return siteSettingRepository.findByKey(key)
                .map(SiteSetting::getValue)
                .orElse(defaultValue);
    }

    // 설정 값 저장/업데이트
    /**
     * 설정값을 저장/업데이트합니다.
     *
     * @param key         설정 키
     * @param value       설정 값
     * @param description 설명(메타정보)
     */
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
