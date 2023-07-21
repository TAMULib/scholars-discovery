package edu.tamu.scholars.middleware.defaults;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import edu.tamu.scholars.middleware.view.model.AnalyticView;
import edu.tamu.scholars.middleware.view.model.repo.AnalyticViewRepo;

@Service
public class AnalyticViewsDefaults extends AbstractDefaults<AnalyticView, AnalyticViewRepo> {

    public AnalyticViewsDefaults() {
        super();
    }

    @Override
    public String path() {
        return "classpath:defaults/analyticViews.yml";
    }

    @Override
    public List<AnalyticView> read(InputStream is) throws IOException {
        List<AnalyticView> views = mapper.readValue(is, new TypeReference<List<AnalyticView>>() {});
        for (AnalyticView view : views) {
            loadTemplateMap(view.getTemplates());
        }
        return views;
    }

}
