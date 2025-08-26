package com.univade.TU.generator.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class FreemarkerConfig {

    @Bean
    public Configuration freemarkerConfiguration() {
        Configuration config = new Configuration(Configuration.VERSION_2_3_32);

        config.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "/templates");

        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setLogTemplateExceptions(false);
        config.setWrapUncheckedExceptions(true);
        config.setFallbackOnNullLoopVariable(false);

        return config;
    }
}
