package edu.tamu.scholars.middleware.defaults;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;
import edu.tamu.scholars.middleware.view.model.DirectoryView;
import edu.tamu.scholars.middleware.view.model.repo.DirectoryViewRepo;

/**
 * 
 */
@Service
public class DirectoryViewsDefaults extends AbstractDefaults<DirectoryView, DirectoryViewRepo> {

    public DirectoryViewsDefaults(
        MiddlewareConfig middleware,
        ResourcePatternResolver resolver,
        DirectoryViewRepo repo
    ) {
        super(middleware, resolver, repo);
    }

    @Override
    public String path() {
        return "classpath:defaults/directoryViews.yml";
    }

    @Override
    public List<DirectoryView> read(InputStream is) throws IOException {
        List<DirectoryView> views = mapper.readValue(is, new TypeReference<List<DirectoryView>>() {});
        for (DirectoryView view : views) {
            loadTemplateMap(view.getTemplates());
        }
        return views;
    }

}
