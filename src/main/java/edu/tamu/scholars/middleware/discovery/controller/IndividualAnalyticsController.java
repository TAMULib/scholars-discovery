package edu.tamu.scholars.middleware.discovery.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/researchAge")
    public ResponseEntity<DiscoveryResearchAge> researcherAge(
        QueryArg query,
        List<FilterArg> filters,
        @RequestParam(name = "label", defaultValue = "Research") String label,
        @RequestParam(name = "dateField", defaultValue = "publicationDate") String dateField,
        @RequestParam(name = "accumulateMultivaluedDate", defaultValue = "false") Boolean accumulateMultivaluedDate,
        // use case: average number of publications out of set of people in age group
        @RequestParam(name = "averageOverInterval", defaultValue = "false") Boolean averageOverInterval,
        @RequestParam(name = "upperLimitInYears", defaultValue = "40") Integer upperLimitInYears,
        @RequestParam(name = "groupingIntervalInYears", defaultValue = "5") Integer groupingIntervalInYears) {

        return ResponseEntity.ok(repo.researcherAge(DiscoveryResearchAgeDescriptor.of(label, dateField, accumulateMultivaluedDate, averageOverInterval, upperLimitInYears, groupingIntervalInYears), query, filters));
    }

}
