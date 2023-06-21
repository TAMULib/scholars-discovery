package edu.tamu.scholars.middleware.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;
import edu.tamu.scholars.middleware.discovery.resolver.BoostArgumentResolver;
import edu.tamu.scholars.middleware.discovery.resolver.FacetArgumentResolver;
import edu.tamu.scholars.middleware.discovery.resolver.FilterArgumentResolver;
import edu.tamu.scholars.middleware.discovery.resolver.HighlightArgumentResolver;
import edu.tamu.scholars.middleware.discovery.resolver.QueryArgumentResolver;
import edu.tamu.scholars.middleware.export.resolver.ExportArgumentResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @Autowired
    private MiddlewareConfig middleware;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/file/**")
            .addResourceLocations(middleware.getAssetsLocation())
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {

                @Override
                @Nullable
                protected Resource getResource(String resourcePath, Resource location) throws IOException {
                    String[] path = resourcePath.split("/");

                    resourcePath = String.format(
                        "a~n/%s/%s/%s",
                        path[0].substring(1, 4),
                        path[0].substring(4, path[0].length()),
                        path[1]
                    );

                    return super.getResource(resourcePath, location);
                }

            });
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/api")
            .setViewName("forward:/api/index.html");
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(900000);
        configurer.setTaskExecutor(taskExecutor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new QueryArgumentResolver());
        resolvers.add(new FilterArgumentResolver());
        resolvers.add(new FacetArgumentResolver());
        resolvers.add(new BoostArgumentResolver());
        resolvers.add(new HighlightArgumentResolver());
        resolvers.add(new ExportArgumentResolver());
    }

}
