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
/**
 * Service class responsible for managing and retrieving insurance policies.
 */
@Slf4j
@Service
public class PolicyService {

    private static List<Policy> policyList = new ArrayList<>();

    /**
     * Loads the list of policies from a JSON file (`insurance_policies.json`) at application startup.
     */
    @PostConstruct
    public void loadPolicies() {
        try {
            log.info("Loading policies from policies.json");
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/insurance_policies.json");
            List<Policy> policies = mapper.readValue(is, new TypeReference<List<Policy>>() {});
            if (!CommonUtils.nullOrEmpty(policies)) {
                policyList = policies;
                log.info("Loaded {} policies", policies.size());
            }
        } catch (Exception e) {
            log.error("Error loading policies from JSON", e);
        }
    }

    /**
     * Returns all policies loaded in memory.
     *
     * @return list of all policies
     */
    public List<Policy> getPolicies() {
        return policyList;
    }

    /**
     * Returns paginated list of all policies.
     *
     * @param page the page number (zero-based)
     * @param size number of policies per page
     * @return sublist of policies for the specified page
     */
    public List<Policy> getPolicies(int page, int size) {
        int start = Math.min(page * size, policyList.size());
        int end = Math.min(start + size, policyList.size());
        log.debug("Returning paginated policies from {} to {}", start, end);
        return policyList.subList(start, end);
    }

    /**
     * Returns paginated list from a custom policy list.
     *
     * @param page     the page number (zero-based)
     * @param size     number of policies per page
     * @param policies the list of policies to paginate
     * @return sublist of policies for the specified page
     */
    public List<Policy> getPolicies(int page, int size, List<Policy> policies) {
        int start = Math.min(page * size, policies.size());
        int end = Math.min(start + size, policies.size());
        log.debug("Returning paginated policies from {} to {}", start, end);
        return policies.subList(start, end);
    }

    /**
     * Returns a paginated response of policies along with total count,
     * optionally filtered by name.
     *
     * @param page               page number (zero-based)
     * @param size               number of items per page
     * @param name               policy name filter (optional)
     * @param totalPagesRequired flag to indicate if total count should be included
     * @return a map containing policies and optionally total count
     */
    public Map<String, Object> getPoliciesWithTotalCount(int page, int size, String name, boolean totalPagesRequired) {
        Map<String, Object> uiOutputMap = new LinkedHashMap<>();

        if (CommonUtils.nullOrEmpty(name)) {
            List<Policy> policies = getPolicies(page, size);
            uiOutputMap.put("policies", policies);

            if (totalPagesRequired)
                uiOutputMap.put("totalPages", getPolicies().size());
        } else {
            List<Policy> policiesByName = getPoliciesByName(policyList, name);
            List<Policy> policies = getPolicies(page, size, policiesByName);
            uiOutputMap.put("policies", policies);
            uiOutputMap.put("totalPages", policiesByName.size());
        }
        return uiOutputMap;
    }

    /**
     * Filters the policy list by matching name (case insensitive).
     *
     * @param policyList list of policies to filter
     * @param name       name or partial name of the policy
     * @return filtered list of policies that match the name
     */
    public List<Policy> getPoliciesByName(List<Policy> policyList, String name) {
        return policyList.stream()
                .filter(policy -> Objects.nonNull(name) &&
                        Objects.nonNull(policy.getName()) &&
                        policy.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    /**
     * Filters policies by various optional criteria: premium range, type, coverage, name, and sort order.
     *
     * @param minPremium minimum premium (inclusive)
     * @param maxPremium maximum premium (inclusive)
     * @param policyType type of policy (e.g., "life", "health")
     * @param minCoverage minimum coverage amount (inclusive)
     * @param sortOrder sorting order by name ("asc" or "desc")
     * @param policyName filter policies by name (case-insensitive)
     * @return filtered and sorted list of policies
     */
    public List<Policy> filterPolicies(Integer minPremium, Integer maxPremium, String policyType, Integer minCoverage, String sortOrder, String policyName) {
        List<Policy> filtered = getPolicies();
        filtered = sortPolicies(filtered, sortOrder);

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
            filtered = getPoliciesByName(filtered, policyName);
            if (filtered.isEmpty()) return filtered;
        }

        return filtered;
    }

    /**
     * Filters a list of policies by premium range.
     *
     * @param policies list of policies
     * @param min minimum premium
     * @param max maximum premium
     * @return filtered list by premium range
     */
    private List<Policy> filterByPremiumRange(List<Policy> policies, Integer min, Integer max) {
        return policies.stream()
                .filter(p -> (min == null || p.getPremium() >= min) &&
                        (max == null || p.getPremium() <= max))
                .collect(Collectors.toList());
    }

    /**
     * Filters policies by type.
     *
     * @param policies list of policies
     * @param type type to filter
     * @return filtered list by type
     */
    private List<Policy> filterByPolicyType(List<Policy> policies, String type) {
        return policies.stream()
                .filter(p -> type == null || p.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * Filters policies by minimum coverage.
     *
     * @param policies list of policies
     * @param minCoverage minimum coverage amount
     * @return filtered list by coverage
     */
    private List<Policy> filterByCoverage(List<Policy> policies, Integer minCoverage) {
        return policies.stream()
                .filter(p -> minCoverage == null || p.getCoverage() >= minCoverage)
                .collect(Collectors.toList());
    }

    /**
     * Sorts the given policy list by name in ascending or descending order.
     *
     * @param policies list of policies
     * @param order sort order ("asc" or "desc")
     * @return sorted list of policies
     */
    private List<Policy> sortPolicies(List<Policy> policies, String order) {
        if (order != null) {
            if ("desc".equalsIgnoreCase(order)) {
                policies = policies.stream()
                        .sorted(Comparator.comparing(Policy::getName).reversed())
                        .toList();
            } else {
                policies = policies.stream()
                        .sorted(Comparator.comparing(Policy::getName))
                        .toList();
            }
        }
        return policies;
    }
}
