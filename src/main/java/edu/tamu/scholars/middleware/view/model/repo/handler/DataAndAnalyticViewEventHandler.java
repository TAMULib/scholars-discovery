package edu.tamu.scholars.middleware.view.model.repo.handler;

import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import edu.tamu.scholars.middleware.view.model.DataAndAnalyticsView;
import edu.tamu.scholars.middleware.view.model.DirectoryView;

@RepositoryEventHandler(DataAndAnalyticsView.class)
public class DataAndAnalyticViewEventHandler extends ViewEventHandler<DirectoryView> {

    public static final String DATA_AND_ANALYTICS_VIEWS_CHANNEL = "/queue/data-and-analytics-views";

    @Override
    protected String getChannel() {
        return DATA_AND_ANALYTICS_VIEWS_CHANNEL;
    }

}
