package edu.tamu.scholars.middleware.defaults;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import edu.tamu.scholars.middleware.view.model.DataAndAnalyticsView;
import edu.tamu.scholars.middleware.view.model.repo.DataAndAnalyticsViewRepo;

@Service
public class AnalyticViewsDefaults extends AbstractDefaults<DataAndAnalyticsView, DataAndAnalyticsViewRepo> {

    public AnalyticViewsDefaults() {
        super();
    }

    @Override
    public String path() {
        return "classpath:defaults/dataAndanalyticViews.yml";
    }

    @Override
    public List<DataAndAnalyticsView> read(InputStream is) throws IOException {
        List<DataAndAnalyticsView> views = mapper.readValue(is, new TypeReference<List<DataAndAnalyticsView>>() {});
        for (DataAndAnalyticsView view : views) {
            loadTemplateMap(view.getTemplates());
        }
        return views;
    }

}
