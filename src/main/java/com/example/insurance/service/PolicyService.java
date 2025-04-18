package com.example.insurance.service;

import com.example.insurance.model.Policy;
import com.example.insurance.util.CommonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PolicyService {

    private static List<Policy> policyList = new ArrayList<>();

    @PostConstruct
    public void loadPolicies() {
        try {
            log.info("Loading policies from policies.json");
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/insurance_policies.json");
            List<Policy> policies = mapper.readValue(is, new TypeReference<List<Policy>>() {
            });

            if (!CommonUtils.nullOrEmpty(policies)) {
                policyList = policies;
                log.info("Loaded {} policies", policies.size());
            }

        } catch (Exception e) {
            log.error("Error loading policies from JSON", e);
        }
    }

    public List<Policy> getPolicies() {
        return policyList;
    }

    public List<Policy> getPolicies(int page, int size) {
        int start = Math.min( (page ) * size, policyList.size());
        int end = Math.min(start + size, policyList.size());
        log.debug("Returning paginated policies from {} to {}", start, end);
        return policyList.subList(start, end);
    }

    public List<Policy> getPolicies(int page, int size , List<Policy> policies) {
        int start = Math.min( (page ) * size, policies.size());
        int end = Math.min(start + size, policies.size());
        log.debug("Returning paginated policies from {} to {}", start, end);
        return policies.subList(start, end);
    }


    public  Map<String , Object> getPoliciesWithTotalCount(int page, int size , String name , boolean totalPagesRequired){
        Map<String , Object> uiOutputMap = new LinkedHashMap<>();

        if (CommonUtils.nullOrEmpty(name)){
            List<Policy> policies = getPolicies(page, size);
            uiOutputMap.put("policies" , policies);

            if (totalPagesRequired)
                    uiOutputMap.put("totalPages" , getPolicies().size());
        }else {

            List<Policy> policiesByName = getPoliciesByName(policyList , name);
            List<Policy> policies = getPolicies(page, size, policiesByName);
            uiOutputMap.put("policies" , policies);
            uiOutputMap.put("totalPages" , policiesByName.size());

        }
        return uiOutputMap;

    }


    public List<Policy> getPoliciesByName(List<Policy> policyList , String name) {
        return policyList.stream()
                .filter(policy -> Objects.nonNull(name) && Objects.nonNull(policy.getName()) && policy.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }


    public List<Policy> filterPolicies(Integer minPremium, Integer maxPremium, String policyType, Integer minCoverage, String sortOrder, String policyName) {
        List<Policy> filtered = getPolicies();

        if (!CommonUtils.nullOrEmpty(maxPremium) && !CommonUtils.nullOrEmpty(minPremium)) {
            filtered = filterByPremiumRange(filtered, minPremium, maxPremium);
            if (filtered.isEmpty()) return filtered;
        }

        if (!CommonUtils.nullOrEmpty(policyType)) {
            filtered = filterByPolicyType(filtered, policyType);
            if (filtered.isEmpty()) return filtered;
        }

        if (!CommonUtils.nullOrEmpty(minCoverage)) {
            filtered = filterByCoverage(filtered, minCoverage);
            if (filtered.isEmpty()) return filtered;
        }

            if (!CommonUtils.nullOrEmpty(policyName)) {
            filtered = getPoliciesByName(filtered , policyName); // full override — not filtering on `filtered`
            if (filtered.isEmpty()) return filtered;
        }

        return sortPolicies(filtered, sortOrder);
    }


    private List<Policy> filterByPremiumRange(List<Policy> policies, Integer min, Integer max) {
        return policies.stream()
                .filter(p -> (min == null || p.getPremium() >= min) &&
                        (max == null || p.getPremium() <= max))
                .collect(Collectors.toList());
    }

    private List<Policy> filterByPolicyType(List<Policy> policies, String type) {
        return policies.stream()
                .filter(p -> type == null || p.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    private List<Policy> filterByCoverage(List<Policy> policies, Integer minCoverage) {
        return policies.stream()
                .filter(p -> minCoverage == null || p.getCoverage() >= minCoverage)
                .collect(Collectors.toList());
    }

    private List<Policy> sortPolicies(List<Policy> policies, String order) {
        if (order != null) {
            if ("desc".equalsIgnoreCase(order)) {
                policies.stream().sorted(Comparator.comparing(Policy::getName).reversed()).toList();
            } else {
                policies = policies.stream()
                        .sorted(Comparator.comparing(Policy::getName))
                        .toList();
            }
        }
        return policies;
    }
}
