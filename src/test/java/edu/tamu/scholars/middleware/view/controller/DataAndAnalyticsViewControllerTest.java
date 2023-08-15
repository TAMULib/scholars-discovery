package edu.tamu.scholars.middleware.view.controller;

import static edu.tamu.scholars.middleware.view.ViewTestUtility.MOCK_VIEW_NAME;
import static edu.tamu.scholars.middleware.view.ViewTestUtility.getMockDataAndAnalyticsView;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.scholars.middleware.auth.model.User;
import edu.tamu.scholars.middleware.utility.ConstraintDescriptionsHelper;
import edu.tamu.scholars.middleware.view.ResourceViewIntegrationTest;
import edu.tamu.scholars.middleware.view.model.DataAndAnalyticsView;
import edu.tamu.scholars.middleware.view.model.repo.DataAndAnalyticsViewRepo;

public class DataAndAnalyticsViewControllerTest extends ResourceViewIntegrationTest<DataAndAnalyticsView, DataAndAnalyticsViewRepo> {

    private static final ConstraintDescriptionsHelper describeDataAndAnalyticsView = new ConstraintDescriptionsHelper(DataAndAnalyticsView.class);

    @Test
    public void testCreateDataAndAnalyticsView() throws JsonProcessingException, Exception {
        // @formatter:off
        performCreateDataAndAnalyticsView()
            .andDo(
                document(
                    "dataAndAnalyticsViews/create",
                    requestFields(
                        describeDataAndAnalyticsView.withField("name", "The name of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("layout", "The layout of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("type", "The container type of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withSubsection("templates", "The result templates of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("styles", "An array of result style strings of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("fields", "An array of fields of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withSubsection("facets", "An array of <<resources-facets, Facet resources>>."),
                        describeDataAndAnalyticsView.withSubsection("filters", "An array of <<resources-filters, Filters resources>>."),
                        describeDataAndAnalyticsView.withSubsection("boosts", "An array of <<resources-boosts, Boosts resources>>."),
                        describeDataAndAnalyticsView.withSubsection("sort", "An array of <<resources-sort, Sort resources>>."),
                        describeDataAndAnalyticsView.withSubsection("export", "An array of <<resources-export, Export resources>>.")
                    ),
                    links(
                        linkWithRel("self").description("Canonical link for this resource."),
                        linkWithRel("dataAndAnalyticsView").description("The Data And Analytics View link for this resource.")
                    ),
                    responseFields(
                        describeDataAndAnalyticsView.withField("name", "The name of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("layout", "The layout of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("type", "The container type of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withSubsection("templates", "The result templates of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("styles", "An array of result style strings of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("fields", "An array of fields of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withSubsection("facets", "An array of <<resources-facets, Facet resources>>."),
                        describeDataAndAnalyticsView.withSubsection("filters", "An array of <<resources-filters, Filters resources>>."),
                        describeDataAndAnalyticsView.withSubsection("boosts", "An array of <<resources-boosts, Boosts resources>>."),
                        describeDataAndAnalyticsView.withSubsection("sort", "An array of <<resources-sort, Sort resources>>."),
                        describeDataAndAnalyticsView.withSubsection("export", "An array of <<resources-export, Export resources>>."),
                        subsectionWithPath("_links").description("<<resources-directory-views-list-links, Links>> to other resources.")
                    )
                )
            );
       // @formatter:on
    }

    @Test
    public void testUpdateDataAndAnalyticsView() throws JsonProcessingException, Exception {
        performCreateDataAndAnalyticsView();

        // @formatter:off
        performUpdateDataAndAnalyticsView()
            .andDo(
                document(
                    "dataAndAnalyticsViews/update",
                    pathParameters(
                        describeDataAndAnalyticsView.withParameter("id", "The Data And Analytics View id.")
                    ),
                    requestFields(
                        describeDataAndAnalyticsView.withField("id", "The Data And Analytics View id."),
                        describeDataAndAnalyticsView.withField("name", "The name of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("layout", "The layout of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("type", "The container type of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withSubsection("templates", "The result templates of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("styles", "An array of result style strings of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("fields", "An array of fields of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withSubsection("facets", "An array of <<resources-facets, Facet resources>>."),
                        describeDataAndAnalyticsView.withSubsection("filters", "An array of <<resources-filters, Filters resources>>."),
                        describeDataAndAnalyticsView.withSubsection("boosts", "An array of <<resources-boosts, Boosts resources>>."),
                        describeDataAndAnalyticsView.withSubsection("sort", "An array of <<resources-sort, Sort resources>>."),
                        describeDataAndAnalyticsView.withSubsection("export", "An array of <<resources-export, Export resources>>.")
                    ),
                    links(
                        linkWithRel("self").description("Canonical link for this resource."),
                        linkWithRel("dataAndAnalyticsView").description("The Data And Analytics View link for this resource.")
                    ),
                    responseFields(
                        describeDataAndAnalyticsView.withField("name", "The name of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("layout", "The layout of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("type", "The container type of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withSubsection("templates", "The result templates of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("styles", "An array of result style strings of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withField("fields", "An array of fields of the Data And Analytics View."),
                        describeDataAndAnalyticsView.withSubsection("facets", "An array of <<resources-facets, Facet resources>>."),
                        describeDataAndAnalyticsView.withSubsection("filters", "An array of <<resources-filters, Filters resources>>."),
                        describeDataAndAnalyticsView.withSubsection("boosts", "An array of <<resources-boosts, Boosts resources>>."),
                        describeDataAndAnalyticsView.withSubsection("sort", "An array of <<resources-sort, Sort resources>>."),
                        describeDataAndAnalyticsView.withSubsection("export", "An array of <<resources-export, Export resources>>."),
                        subsectionWithPath("_links").description("<<resources-directory-views-list-links, Links>> to other resources.")
                    )
                )
            );
        // @formatter:on
    }

    @Test
    public void testPatchTheme() throws JsonProcessingException, Exception {
        performCreateDataAndAnalyticsView();
        DataAndAnalyticsView dataAndAnalyticsView = viewRepo.findByName(MOCK_VIEW_NAME).get();

        // @formatter:off
        mockMvc.perform(
            patch("/dataAndAnalyticsViews/{id}", dataAndAnalyticsView.getId())
                .content("{\"name\": \"Organizations\"}")
                .cookie(loginAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(HAL_JSON_VALUE))
                    .andExpect(jsonPath("name", equalTo("Organizations")))
                    .andDo(
                        document(
                            "dataAndAnalyticsViews/patch",
                            pathParameters(
                                describeDataAndAnalyticsView.withParameter("id", "The Data And Analytics View id.")
                            ),
                            requestParameters(
                                describeDataAndAnalyticsView.withParameter("id", "The Data And Analytics View id.").optional(),
                                describeDataAndAnalyticsView.withParameter("name", "The name of the Data And Analytics View.").optional(),
                                describeDataAndAnalyticsView.withParameter("layout", "The layout of the Data And Analytics View.").optional(),
                                describeDataAndAnalyticsView.withParameter("type", "The container type of the Data And Analytics View.").optional(),
                                describeDataAndAnalyticsView.withParameter("templates", "The result templates of the Data And Analytics View.").optional(),
                                describeDataAndAnalyticsView.withParameter("styles", "An array of result style strings of the Data And Analytics View.").optional(),
                                describeDataAndAnalyticsView.withParameter("fields", "An array of fields of the Data And Analytics View.").optional(),
                                describeDataAndAnalyticsView.withParameter("facets", "An array of <<resources-facets, Facet resources>>.").optional(),
                                describeDataAndAnalyticsView.withParameter("filters", "An array of <<resources-filters, Filters resources>>.").optional(),
                                describeDataAndAnalyticsView.withParameter("boosts", "An array of <<resources-boosts, Boosts resources>>.").optional(),
                                describeDataAndAnalyticsView.withParameter("sort", "An array of <<resources-sort, Sort resources>>.").optional(),
                                describeDataAndAnalyticsView.withParameter("export", "An array of <<resources-export, Export resources>>.").optional()                                
                            ),
                            links(
                                linkWithRel("self").description("Canonical link for this resource."),
                                linkWithRel("dataAndAnalyticsView").description("The Data And Analytics View link for this resource.")
                            ),
                            responseFields(
                                describeDataAndAnalyticsView.withField("name", "The name of the Data And Analytics View."),
                                describeDataAndAnalyticsView.withField("layout", "The layout of the Data And Analytics View."),
                                describeDataAndAnalyticsView.withField("type", "The container type of the Data And Analytics View."),
                                describeDataAndAnalyticsView.withSubsection("templates", "The result templates of the Data And Analytics View."),
                                describeDataAndAnalyticsView.withField("styles", "An array of result style strings of the Data And Analytics View."),
                                describeDataAndAnalyticsView.withField("fields", "An array of fields of the Data And Analytics View."),
                                describeDataAndAnalyticsView.withSubsection("facets", "An array of <<resources-facets, Facet resources>>."),
                                describeDataAndAnalyticsView.withSubsection("filters", "An array of <<resources-filters, Filters resources>>."),
                                describeDataAndAnalyticsView.withSubsection("boosts", "An array of <<resources-boosts, Boosts resources>>."),
                                describeDataAndAnalyticsView.withSubsection("sort", "An array of <<resources-sort, Sort resources>>."),
                                describeDataAndAnalyticsView.withSubsection("export", "An array of <<resources-export, Export resources>>."),
                                subsectionWithPath("_links").description("<<resources-directory-views-list-links, Links>> to other resources.")
                            )
                        )
                    );
        // @formatter:on
    }

    @Test
    public void testGetDataAndAnalyticsView() throws JsonProcessingException, Exception {
        performCreateDataAndAnalyticsView();
        DataAndAnalyticsView dataAndAnalyticsView = viewRepo.findByName(MOCK_VIEW_NAME).get();
        // @formatter:off
        mockMvc.perform(
            get("/dataAndAnalyticsViews/{id}", dataAndAnalyticsView.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(HAL_JSON_VALUE))
                .andExpect(jsonPath("name", equalTo(MOCK_VIEW_NAME)))
                .andDo(
                    document(
                        "dataAndAnalyticsViews/find-by-id",
                        pathParameters(
                            describeDataAndAnalyticsView.withParameter("id", "The Theme id.")
                        ),
                        links(
                            linkWithRel("self").description("Canonical link for this resource."),
                            linkWithRel("dataAndAnalyticsView").description("The Data And Analytics View link for this resource.")
                        ),
                        responseFields(
                            describeDataAndAnalyticsView.withField("name", "The name of the Data And Analytics View."),
                            describeDataAndAnalyticsView.withField("layout", "The layout of the Data And Analytics View."),
                            describeDataAndAnalyticsView.withField("type", "The container type of the Data And Analytics View."),
                            describeDataAndAnalyticsView.withSubsection("templates", "The result templates of the Data And Analytics View."),
                            describeDataAndAnalyticsView.withField("styles", "An array of result style strings of the Data And Analytics View."),
                            describeDataAndAnalyticsView.withField("fields", "An array of fields of the Data And Analytics View."),
                            describeDataAndAnalyticsView.withSubsection("facets", "An array of <<resources-facets, Facet resources>>."),
                            describeDataAndAnalyticsView.withSubsection("filters", "An array of <<resources-filters, Filters resources>>."),
                            describeDataAndAnalyticsView.withSubsection("boosts", "An array of <<resources-boosts, Boosts resources>>."),
                            describeDataAndAnalyticsView.withSubsection("sort", "An array of <<resources-sort, Sort resources>>."),
                            describeDataAndAnalyticsView.withSubsection("export", "An array of <<resources-export, Export resources>>."),
                            subsectionWithPath("_links").description("<<resources-directory-view-list-links, Links>> to other resources.")
                        )
                    )
                );
        // @formatter:on
    }

    @Test
    public void testGetDataAndAnalyticsViews() throws JsonProcessingException, Exception {
        performCreateDataAndAnalyticsView();
        // @formatter:off
        mockMvc.perform(
            get("/dataAndAnalyticsViews").param("page", "0").param("size", "20").param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(HAL_JSON_VALUE))
                .andExpect(jsonPath("page.size", equalTo(20)))
                .andExpect(jsonPath("page.totalElements", equalTo(1)))
                .andExpect(jsonPath("page.totalPages", equalTo(1)))
                .andExpect(jsonPath("page.number", equalTo(1)))
                .andExpect(jsonPath("_embedded.dataAndAnalyticsViews[0].name", equalTo(MOCK_VIEW_NAME)))
                .andDo(
                    document(
                        "dataAndAnalyticsViews/directory",
                        requestParameters(
                            parameterWithName("page").description("The page number."),
                            parameterWithName("size").description("The page size."),
                            parameterWithName("sort").description("The page sort.")
                        ),
                        links(
                            linkWithRel("self").description("Canonical link for this resource."),
                            linkWithRel("profile").description("The ALPS profile for this resource.")
                        ),
                        responseFields(
                            subsectionWithPath("_embedded.dataAndAnalyticsViews").description("An array of <<resources-directory-views, Data And Analytics View resources>>."),
                            subsectionWithPath("_links").description("<<resources-directory-view-list-links, Links>> to other resources."),
                            subsectionWithPath("page").description("Page details for <<resources-directory-views, Data And Analytics View resources>>.")
                        )
                    )
                );
        // @formatter:on
    }

    @Test
    public void testDeleteTheme() throws JsonProcessingException, Exception {
        performCreateDataAndAnalyticsView();
        DataAndAnalyticsView dataAndAnalyticsView = viewRepo.findByName(MOCK_VIEW_NAME).get();
        // @formatter:off
        mockMvc.perform(
            delete("/dataAndAnalyticsViews/{id}", dataAndAnalyticsView.getId())
                .cookie(loginAdmin()))
                    .andExpect(status().isNoContent())
                    .andDo(
                        document(
                            "dataAndAnalyticsViews/delete",
                            pathParameters(
                                parameterWithName("id").description("The Data And Analytics View id")
                            )
                        )
                    );
        // @formatter:on
    }

    protected Cookie loginAdmin() throws Exception {
        User user = getMockAdmin();
        MvcResult directory = mockMvc.perform(post("/login").param("username", user.getEmail()).param("password", "HelloWorld123!")).andReturn();
        return directory.getResponse().getCookie("SESSION");
    }

    private ResultActions performCreateDataAndAnalyticsView() throws JsonProcessingException, Exception {
        createMockAdmin();
        DataAndAnalyticsView dataAndAnalyticsView = getMockView();

        // @formatter:off
        return mockMvc.perform(
            post("/dataAndAnalyticsViews")
                .content(objectMapper.writeValueAsString(dataAndAnalyticsView))
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(loginAdmin()))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(HAL_JSON_VALUE)
            );
        // @formatter:on
    }

    private ResultActions performUpdateDataAndAnalyticsView() throws JsonProcessingException, Exception {
        DataAndAnalyticsView dataAndAnalyticsView = viewRepo.findByName(MOCK_VIEW_NAME).get();
        dataAndAnalyticsView.setName("Organizations");
        // dataAndAnalyticsView.setCollection("organizations");

        // @formatter:off
        return mockMvc.perform(
            put("/dataAndAnalyticsViews/{id}", dataAndAnalyticsView.getId())
                .content(objectMapper.writeValueAsString(dataAndAnalyticsView))
                .cookie(loginAdmin()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(HAL_JSON_VALUE)
            );
        // @formatter:on
    }

    @Override
    protected DataAndAnalyticsView getMockView() {
        return getMockDataAndAnalyticsView();
    }

}
