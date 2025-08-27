package edu.tamu.scholars.middleware.defaults;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import edu.tamu.scholars.middleware.config.model.MiddlewareConfig;
import edu.tamu.scholars.middleware.view.model.DisplaySectionView;
import edu.tamu.scholars.middleware.view.model.DisplaySubsectionView;
import edu.tamu.scholars.middleware.view.model.DisplayTabView;
import edu.tamu.scholars.middleware.view.model.DisplayView;
import edu.tamu.scholars.middleware.view.model.ExportView;
import edu.tamu.scholars.middleware.view.model.repo.DisplayViewRepo;

/**
 * 
 */
@Service
public class DisplayViewDefaults extends AbstractDefaults<DisplayView, DisplayViewRepo> {

    public DisplayViewDefaults(
        MiddlewareConfig middleware,
        ResourcePatternResolver resolver,
        DisplayViewRepo repo
    ) {
        super(middleware, resolver, repo);
    }

    @Override
    public String path() {
        return "classpath:defaults/displayViews.yml";
    }

    @Override
    public List<DisplayView> read(InputStream is) throws IOException {
        List<DisplayView> views = mapper.readValue(is, new TypeReference<List<DisplayView>>() {});
        for (DisplayView view : views) {
            if (view.getMainContentTemplate() != null && !view.getMainContentTemplate().isEmpty()) {
                try {
                    view.setMainContentTemplate(getTemplate(view.getMainContentTemplate()));
                } catch (IOException e) {
                    logger.warn(String.format(IO_EXCEPTION_MESSAGE, view.getMainContentTemplate()));
                }
            }
            if (view.getLeftScanTemplate() != null && !view.getLeftScanTemplate().isEmpty()) {
                try {
                    view.setLeftScanTemplate(getTemplate(view.getLeftScanTemplate()));
                } catch (IOException e) {
                    logger.warn(String.format(IO_EXCEPTION_MESSAGE, view.getLeftScanTemplate()));
                }
            }
            if (view.getRightScanTemplate() != null && !view.getRightScanTemplate().isEmpty()) {
                try {
                    view.setRightScanTemplate(getTemplate(view.getRightScanTemplate()));
                } catch (IOException e) {
                    logger.warn(String.format(IO_EXCEPTION_MESSAGE, view.getRightScanTemplate()));
                }
            }
            if (view.getAsideTemplate() != null && !view.getAsideTemplate().isEmpty()) {
                try {
                    view.setAsideTemplate(getTemplate(view.getAsideTemplate()));
                } catch (IOException e) {
                    logger.warn(String.format(IO_EXCEPTION_MESSAGE, view.getAsideTemplate()));
                }
            }
            if (view.getTabs() != null) {
                for (DisplayTabView tabView : view.getTabs()) {
                    if (tabView.getSections() != null) {
                        for (DisplaySectionView section : tabView.getSections()) {
                            section.setTemplate(getTemplate(section.getTemplate()));
                            if (section.getSubsections() != null) {
                                for (DisplaySubsectionView subsection : section.getSubsections()) {
                                    subsection.setTemplate(getTemplate(subsection.getTemplate()));
                                }
                            }
                        }
                    }
                }
            }

            for (ExportView exportView : view.getExportViews()) {
                if (exportView.getContentTemplate() != null && !exportView.getContentTemplate().isEmpty()) {
                    try {
                        exportView.setContentTemplate(getTemplate(exportView.getContentTemplate()));
                    } catch (IOException e) {
                        logger.warn(String.format(IO_EXCEPTION_MESSAGE, exportView.getContentTemplate()));
                    }
                }
                if (exportView.getHeaderTemplate() != null && !exportView.getHeaderTemplate().isEmpty()) {
                    try {
                        exportView.setHeaderTemplate(getTemplate(exportView.getHeaderTemplate()));
                    } catch (IOException e) {
                        logger.warn(String.format(IO_EXCEPTION_MESSAGE, exportView.getHeaderTemplate()));
                    }
                }
            }
            loadTemplateMap(view.getMetaTemplates());
            loadTemplateMap(view.getEmbedTemplates());
        }
        return views;
    }

    private String getTemplate(String path) throws IOException {
        Resource resource = resolver.getResource(String.format(CLASSPATH, path));
        if (resource.exists()) {
            return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        }
        throw new IOException(String.format(IO_EXCEPTION_MESSAGE, path));
    }

}
