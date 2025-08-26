package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.SmsTemplate;
import com.jerrycode.gym_services.utils.Language;
import com.jerrycode.gym_services.utils.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {
    SmsTemplate findByName(TemplateType name);
    SmsTemplate findByNameAndLanguage(TemplateType name, Language language);
}
