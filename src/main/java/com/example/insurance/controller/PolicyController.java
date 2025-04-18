package com.example.insurance.controller;
import com.example.insurance.model.Policy;
import com.example.insurance.service.PolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * REST controller for handling insurance policy-related requests.
 */
@Slf4j
@RestController
@RequestMapping("/policies")
public class PolicyController {

    private final PolicyService policyService;

    /**
     * Constructor for injecting the PolicyService.
     *
     * @param policyService service class that handles policy operations
     */
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    /**
     * Retrieves a paginated list of policies, with optional name filtering and total count.
     *
     * @param page                page number (default is 0)
     * @param size                number of policies per page (default is 10)
     * @param totalPagesRequired  flag to include total policy count in response
     * @param name                optional filter for policy name
     * @return map containing paginated policies and optionally total count
     */
    @GetMapping
    public Map<String, Object> getPolicies(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "false", required = false) boolean totalPagesRequired,
                                           @RequestParam(required = false) String name) {
        log.info("Fetching policies for page={}, size={}, totalPagesRequired={}, name={}", page, size, totalPagesRequired, name);
        return policyService.getPoliciesWithTotalCount(page, size, name, totalPagesRequired);
    }

    /**
     * Retrieves a list of policies that match the given name (case-insensitive, partial match).
     *
     * @param name name or partial name of the policy
     * @return list of matching policies
     */
    @GetMapping("/search")
    public List<Policy> getPoliciesByName(@RequestParam(required = false) String name) {
        log.info("Searching policies by name={}", name);
        return policyService.getPoliciesByName(policyService.getPolicies(), name);
    }

    /**
     * Filters policies based on various optional criteria such as premium, type, coverage, name, and sort order.
     *
     * @param minPremium minimum premium (inclusive)
     * @param maxPremium maximum premium (inclusive)
     * @param policyType policy type to filter
     * @param name       name of the policy to filter
     * @param coverage   minimum coverage amount (inclusive)
     * @param sortOrder  sort order by name ("asc" or "desc"), default is "aesc" (typo for "asc"?)
     * @return list of filtered policies
     */
    @GetMapping("/filter")
    public List<Policy> getFilteredPolicies(@RequestParam(required = false) Integer minPremium,
                                            @RequestParam(required = false) Integer maxPremium,
                                            @RequestParam(required = false) String policyType,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Integer coverage,
                                            @RequestParam(required = false, defaultValue = "aesc") String sortOrder) {
        log.info("Filtering policies with minPremium={}, maxPremium={}, policyType={}, coverage={}, sortOrder={}, name={}",
                minPremium, maxPremium, policyType, coverage, sortOrder, name);
        return policyService.filterPolicies(minPremium, maxPremium, policyType, coverage, sortOrder, name);
    }

    /**
     * Returns all available unique policy types.
     *
     * @return set of unique policy types
     */
    @GetMapping("/getPolicyTypes")
    public Set<String> getPolicyTypes() {
        log.info("getPolicy types");
        return policyService.getPolicies().stream()
                .map(Policy::getType)
                .collect(Collectors.toSet());
    }
}
