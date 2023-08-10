package edu.tamu.scholars.middleware.view.model.repo;

import static edu.tamu.scholars.middleware.view.ViewTestUtility.getMockDataAndAnalyticsView;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;
import edu.tamu.scholars.middleware.view.model.DataAndAnalyticsView;

public class DataAndAnalyticsViewRepoTest extends ViewRepoTest<DataAndAnalyticsView, DataAndAnalyticsViewRepo> {

    @TestConfiguration
    static class DataAndAnalyticsViewRepoTestContextConfiguration {

        @Bean
        public MiddlewareConfig middlewareConfig() {
            return new MiddlewareConfig();
        }

        @Bean
        public BCryptPasswordEncoder bCryptPasswordEncoder() {
            return new BCryptPasswordEncoder();
        }

    }

    @Override
    protected DataAndAnalyticsView getMockView() {
        return getMockDataAndAnalyticsView();
    }

}
