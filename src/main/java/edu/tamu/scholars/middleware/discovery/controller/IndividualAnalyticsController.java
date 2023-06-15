package edu.tamu.scholars.middleware.discovery.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.scholars.middleware.discovery.argument.DiscoveryResearchAgeDescriptor;
import edu.tamu.scholars.middleware.discovery.argument.FilterArg;
import edu.tamu.scholars.middleware.discovery.argument.QueryArg;
import edu.tamu.scholars.middleware.discovery.model.repo.IndividualRepo;
import edu.tamu.scholars.middleware.discovery.response.DiscoveryResearchAge;

@RestController
@RequestMapping("/individual/analytics")
public class IndividualAnalyticsController {

    @Autowired
    private IndividualRepo repo;

    @GetMapping("/researchAge")
    public ResponseEntity<DiscoveryResearchAge> researcherAge(
        QueryArg query,
        List<FilterArg> filters,
        @RequestParam(name = "dateField", defaultValue = "publicationDate") String dateField,
        @RequestParam(name = "accumulateMultivaluedDate", defaultValue = "false") Boolean accumulateMultivaluedDate,
        @RequestParam(name = "upperLimitInYears", defaultValue = "40") Integer upperLimitInYears,
        @RequestParam(name = "groupingIntervalInYears", defaultValue = "5") Integer groupingIntervalInYears

    ) {
        try {
            ObjectMapper om = new ObjectMapper();
            System.out.println("QUERY: " + om.writerWithDefaultPrettyPrinter().writeValueAsString(query));
        } catch (JsonProcessingException e) {

        }
        return ResponseEntity.ok(repo.researcherAge(DiscoveryResearchAgeDescriptor.of(dateField, accumulateMultivaluedDate, upperLimitInYears, groupingIntervalInYears), query, filters));
    }

}
