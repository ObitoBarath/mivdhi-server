package com.example.insurance.controller;
import com.example.insurance.model.Policy;
import com.example.insurance.service.PolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/policies")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping
    public Map<String , Object> getPolicies(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "false" , required = false) boolean totalPagesRequired,
                                            @RequestParam( required = false) String name
    ) {
        log.info("Fetching policies for page={}, size={}, totalPagesRequired {}, name {}", page, size ,totalPagesRequired,name);

        return policyService.getPoliciesWithTotalCount(page ,size ,name , totalPagesRequired);
    }



    @GetMapping("/search")
    public List<Policy> getPoliciesByName(@RequestParam(required = false) String name) {
        log.info("Searching policies by name={}", name);
        return policyService.getPoliciesByName(policyService.getPolicies() ,name);
    }

    @GetMapping("/filter")
    public List<Policy> getFilteredPolicies(@RequestParam(required = false) Integer minPremium,
                                            @RequestParam(required = false) Integer maxPremium,
                                            @RequestParam(required = false) String policyType,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Integer coverage,
                                            @RequestParam(required = false , defaultValue = "aesc") String sortOrder) {
        log.info("Filtering policies with minPremium={}, maxPremium={}, policyType={}, coverage={}, sortOrder={} name={}",
                minPremium, maxPremium, policyType, coverage, sortOrder , name);
        return policyService.filterPolicies(minPremium, maxPremium, policyType, coverage, sortOrder , name);
    }

    @GetMapping("/getPolicyTypes")
    public Set<String> getPolicyTypes() {
        log.info("getPolicy types");
        return policyService.getPolicies().stream().map(Policy::getType).collect(Collectors.toSet());
    }





}
