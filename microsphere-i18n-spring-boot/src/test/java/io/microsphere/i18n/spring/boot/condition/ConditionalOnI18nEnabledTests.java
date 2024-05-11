package io.microsphere.i18n.spring.boot.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ConditionalOnI18nEnabledTests {

    ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void setup() {
        applicationContextRunner = new ApplicationContextRunner();
    }

    @Test
    void shouldEnableI18nByDefault() {
        applicationContextRunner.withUserConfiguration(Config.class)
                .run(context -> assertTrue(context.containsBean("a")));
    }

    @Test
    void shouldEnableI18nWhenPropertyIsTrue() {
        applicationContextRunner.withUserConfiguration(Config.class)
                .withPropertyValues("microsphere.i18n.enabled=true")
                .run(context -> assertTrue(context.containsBean("a")));
    }

    @Test
    void shouldDisableI18nWhenPropertyIsFalse() {
        applicationContextRunner.withUserConfiguration(Config.class)
                .withPropertyValues("microsphere.i18n.enabled=false")
                .run(context -> assertFalse(context.containsBean("a")));
    }
    @Test
    void shouldDisableI18nWhenMissingClass() {
        applicationContextRunner.withUserConfiguration(Config.class)
                .withPropertyValues("microsphere.i18n.enabled=true")
                .withClassLoader(new FilteredClassLoader(
                        "io.microsphere.i18n.ServiceMessageSource",
                        "io.microsphere.i18n.spring.annotation.EnableI18n"))
                .run(context -> assertFalse(context.containsBean("a")));
    }

    @Configuration
    static class Config {

        @Bean
        @ConditionalOnI18nEnabled
        public String a() {
            return "a";
        }
    }
}
