package edu.tamu.scholars.middleware.view.model;

import static edu.tamu.scholars.middleware.view.ViewTestUtility.MOCK_VIEW_NAME;
import static edu.tamu.scholars.middleware.view.ViewTestUtility.getMockDataAndAnalyticsView;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class DataAndAnalyticsViewTest {

    @Test
    public void testDefaultConstructor() {
        DataAndAnalyticsView dataAndAnalyticsView = new DataAndAnalyticsView();
        assertNotNull(dataAndAnalyticsView);
        assertNotNull(dataAndAnalyticsView.getFacets());
        assertNotNull(dataAndAnalyticsView.getFilters());
        assertTrue(dataAndAnalyticsView.getFacets().isEmpty());
        assertTrue(dataAndAnalyticsView.getFilters().isEmpty());
        assertTrue(dataAndAnalyticsView.getBoosts().isEmpty());
        assertTrue(dataAndAnalyticsView.getSort().isEmpty());
        assertTrue(dataAndAnalyticsView.getExport().isEmpty());
    }

    @Test
    public void testGettersAndSetters() {
        DataAndAnalyticsView dataAndAnalyticsView = getMockDataAndAnalyticsView();
        dataAndAnalyticsView.setId(1L);

        assertEquals(1L, dataAndAnalyticsView.getId(), 1);
        assertEquals(MOCK_VIEW_NAME, dataAndAnalyticsView.getName());
        assertEquals(Layout.GRID, dataAndAnalyticsView.getLayout());
        assertEquals(ContainerType.PROFILE_SUMMARIES_EXPORT, dataAndAnalyticsView.getType());

        assertTrue(dataAndAnalyticsView.getTemplates().containsKey("default"));
        assertEquals("<h1>Element template from WSYWIG</h1>", dataAndAnalyticsView.getTemplates().get("default"));

        assertEquals(1, dataAndAnalyticsView.getStyles().size());
        assertEquals("color: maroon;", dataAndAnalyticsView.getStyles().get(0));

        assertEquals(1, dataAndAnalyticsView.getFields().size());
        assertEquals("title", dataAndAnalyticsView.getFields().get(0));

        assertEquals(1, dataAndAnalyticsView.getFacets().size());
        assertEquals("Name", dataAndAnalyticsView.getFacets().get(0).getName());
        assertEquals("name", dataAndAnalyticsView.getFacets().get(0).getField());
        assertEquals(FacetType.STRING, dataAndAnalyticsView.getFacets().get(0).getType());
        assertEquals(FacetSort.COUNT, dataAndAnalyticsView.getFacets().get(0).getSort());
        assertEquals(Sort.Direction.DESC, dataAndAnalyticsView.getFacets().get(0).getDirection());
        assertEquals(20, dataAndAnalyticsView.getFacets().get(0).getPageSize());
        assertEquals(1, dataAndAnalyticsView.getFacets().get(0).getPageNumber());
        assertTrue(dataAndAnalyticsView.getFacets().get(0).isCollapsed());
        assertFalse(dataAndAnalyticsView.getFacets().get(0).isHidden());

        assertEquals(1, dataAndAnalyticsView.getFilters().size());
        assertEquals("type", dataAndAnalyticsView.getFilters().get(0).getField());
        assertEquals("FacultyMember", dataAndAnalyticsView.getFilters().get(0).getValue());

        assertEquals(1, dataAndAnalyticsView.getBoosts().size());
        assertEquals("name", dataAndAnalyticsView.getBoosts().get(0).getField());
        assertEquals(2.0f, dataAndAnalyticsView.getBoosts().get(0).getValue());

        assertEquals(1, dataAndAnalyticsView.getSort().size());
        assertEquals("name", dataAndAnalyticsView.getSort().get(0).getField());
        assertEquals(Direction.ASC, dataAndAnalyticsView.getSort().get(0).getDirection());

        assertEquals(2, dataAndAnalyticsView.getExport().size());
        assertEquals("Id", dataAndAnalyticsView.getExport().get(0).getColumnHeader());
        assertEquals("id", dataAndAnalyticsView.getExport().get(0).getValuePath());
        assertEquals("Name", dataAndAnalyticsView.getExport().get(1).getColumnHeader());
        assertEquals("name", dataAndAnalyticsView.getExport().get(1).getValuePath());
    }

}
