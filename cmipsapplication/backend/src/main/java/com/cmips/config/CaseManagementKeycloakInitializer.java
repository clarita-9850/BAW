package com.cmips.config;

import com.cmips.service.KeycloakAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Initializes Keycloak resources and permissions for Case Management system
 * Creates resources, scopes, roles, and policies for:
 * - Case Management (DSD Section 20)
 * - Service Eligibility (DSD Section 21)
 * - Provider Management (DSD Section 23)
 */
@Component
@Order(2)
public class CaseManagementKeycloakInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CaseManagementKeycloakInitializer.class);

    private final KeycloakAdminService keycloakAdminService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${cmips.keycloak.init-resources:false}")
    private boolean initResources;

    public CaseManagementKeycloakInitializer(KeycloakAdminService keycloakAdminService) {
        this.keycloakAdminService = keycloakAdminService;
    }

    @Override
    public void run(String... args) {
        if (!initResources) {
            log.info("Keycloak resource initialization is disabled. Set cmips.keycloak.init-resources=true to enable.");
            return;
        }

        log.info("Initializing Keycloak resources for Case Management system...");

        try {
            // Create roles
            createRoles();

            // Create resources with scopes
            createResources();

            log.info("Keycloak resource initialization completed successfully");

        } catch (Exception e) {
            log.error("Error initializing Keycloak resources: {}", e.getMessage(), e);
        }
    }

    private void createRoles() {
        log.info("Creating case management roles...");

        // Case Management roles
        createRoleIfNotExists("case-viewer", "Can view case information");
        createRoleIfNotExists("case-creator", "Can create new cases and referrals");
        createRoleIfNotExists("case-editor", "Can edit case information");
        createRoleIfNotExists("case-approver", "Can approve/deny cases");
        createRoleIfNotExists("case-supervisor", "Supervisor with full case management access");
        createRoleIfNotExists("case-transfer-manager", "Can manage inter-county transfers");

        // Provider Management roles
        createRoleIfNotExists("provider-viewer", "Can view provider information");
        createRoleIfNotExists("provider-creator", "Can create new providers");
        createRoleIfNotExists("provider-editor", "Can edit provider information");
        createRoleIfNotExists("provider-approver", "Can approve provider enrollment");
        createRoleIfNotExists("provider-supervisor", "Supervisor with full provider management access");
        createRoleIfNotExists("cori-viewer", "Can view CORI information");
        createRoleIfNotExists("cori-manager", "Can manage CORI records and exceptions");
        createRoleIfNotExists("violation-reviewer", "Can review overtime violations");
        createRoleIfNotExists("violation-supervisor-reviewer", "Can perform supervisor review on violations");

        // Recipient Management roles
        createRoleIfNotExists("recipient-viewer", "Can view recipient information");
        createRoleIfNotExists("recipient-creator", "Can create new recipients");
        createRoleIfNotExists("recipient-editor", "Can edit recipient information");
        createRoleIfNotExists("referral-manager", "Can manage referrals");

        // Service Eligibility roles
        createRoleIfNotExists("eligibility-viewer", "Can view service eligibility");
        createRoleIfNotExists("eligibility-assessor", "Can create and edit assessments");
        createRoleIfNotExists("health-cert-viewer", "Can view health care certifications");
        createRoleIfNotExists("health-cert-manager", "Can manage health care certifications");
        createRoleIfNotExists("health-cert-approver", "Can approve health care certification exceptions");

        // County-specific roles
        createRoleIfNotExists("county-admin", "County administrator with access to county-specific data");
        createRoleIfNotExists("state-admin", "State administrator with access to all data");

        log.info("Role creation completed");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        try {
            if (keycloakAdminService.getRoleByName(roleName) == null) {
                keycloakAdminService.createRole(roleName, description);
                log.info("Created role: {}", roleName);
            } else {
                log.debug("Role already exists: {}", roleName);
            }
        } catch (Exception e) {
            log.warn("Could not create role {}: {}", roleName, e.getMessage());
        }
    }

    private void createResources() {
        log.info("Creating case management resources...");

        // Case Resource
        createResource("Case Resource", "Case management resource",
            Arrays.asList("view", "create", "edit", "approve", "deny", "terminate", "assign", "transfer"),
            getCaseFieldPermissions());

        // Case Notes Resource
        createResource("Case Notes Resource", "Case notes management",
            Arrays.asList("view", "create", "edit", "delete"),
            getCaseNotesFieldPermissions());

        // Case Contacts Resource
        createResource("Case Contacts Resource", "Case contacts management",
            Arrays.asList("view", "create", "edit", "delete"),
            null);

        // Recipient Resource
        createResource("Recipient Resource", "Recipient/Person management",
            Arrays.asList("view", "create", "edit"),
            getRecipientFieldPermissions());

        // Referral Resource
        createResource("Referral Resource", "Referral management",
            Arrays.asList("view", "create", "close", "reopen"),
            null);

        // Provider Resource
        createResource("Provider Resource", "Provider management",
            Arrays.asList("view", "create", "edit", "approve", "enroll", "reinstate"),
            getProviderFieldPermissions());

        // Provider Assignment Resource
        createResource("Provider Assignment Resource", "Provider-case assignment management",
            Arrays.asList("view", "create", "edit", "terminate"),
            null);

        // Provider CORI Resource
        createResource("Provider CORI Resource", "Provider CORI background check management",
            Arrays.asList("view", "create", "edit"),
            getCoriFieldPermissions());

        // Overtime Violation Resource
        createResource("Overtime Violation Resource", "Overtime violation tracking",
            Arrays.asList("view", "create", "edit", "review", "supervisor-review"),
            null);

        // Service Eligibility Resource
        createResource("Service Eligibility Resource", "Service eligibility and assessments",
            Arrays.asList("view", "create", "edit"),
            getEligibilityFieldPermissions());

        // Health Care Certification Resource
        createResource("Health Care Certification Resource", "SOC 873 health care certification",
            Arrays.asList("view", "create", "edit", "approve"),
            null);

        log.info("Resource creation completed");
    }

    private void createResource(String name, String description, List<String> scopes,
                                 Map<String, List<String>> fieldPermissions) {
        try {
            String token = getAdminToken();
            String clientInternalId = getBackendClientInternalId(token);

            // Check if resource already exists
            List<Map<String, Object>> existingResources = keycloakAdminService.getAllResources();
            boolean exists = existingResources.stream()
                .anyMatch(r -> name.equals(r.get("name")));

            if (exists) {
                log.debug("Resource already exists: {}", name);

                // Update field permissions if provided
                if (fieldPermissions != null) {
                    String resourceId = existingResources.stream()
                        .filter(r -> name.equals(r.get("name")))
                        .map(r -> (String) r.get("_id"))
                        .findFirst()
                        .orElse(null);

                    if (resourceId != null) {
                        keycloakAdminService.updateResourceAttributes(resourceId, fieldPermissions);
                    }
                }
                return;
            }

            // Create resource
            Map<String, Object> resourceRequest = new HashMap<>();
            resourceRequest.put("name", name);
            resourceRequest.put("displayName", name);
            resourceRequest.put("type", "urn:cmips:resources:" + name.toLowerCase().replace(" ", "-"));

            // Add scopes
            List<Map<String, String>> scopeList = new ArrayList<>();
            for (String scope : scopes) {
                Map<String, String> scopeMap = new HashMap<>();
                scopeMap.put("name", scope);
                scopeList.add(scopeMap);
            }
            resourceRequest.put("scopes", scopeList);

            // Add field-level permissions as attributes
            if (fieldPermissions != null) {
                resourceRequest.put("attributes", fieldPermissions);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(resourceRequest, headers);
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId +
                        "/authz/resource-server/resource";

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Created resource: {}", name);
            }

        } catch (Exception e) {
            log.warn("Could not create resource {}: {}", name, e.getMessage());
        }
    }

    // Field-level permission configurations

    private Map<String, List<String>> getCaseFieldPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();

        // All roles can see basic fields
        permissions.put("fields.basic", Arrays.asList(
            "id", "caseNumber", "caseStatus", "caseType", "countyCode", "caseOwnerId"
        ));

        // Case viewers can see more details
        permissions.put("fields.case-viewer", Arrays.asList(
            "id", "caseNumber", "caseStatus", "caseType", "countyCode", "caseOwnerId",
            "recipientId", "cin", "referralDate", "applicationDate", "eligibilityDate",
            "authorizedHoursMonthly", "authorizedHoursWeekly", "assessmentType"
        ));

        // Case supervisors can see sensitive fields
        permissions.put("fields.case-supervisor", Arrays.asList(
            "id", "caseNumber", "caseStatus", "caseType", "countyCode", "caseOwnerId",
            "recipientId", "cin", "referralDate", "applicationDate", "eligibilityDate",
            "authorizationStartDate", "authorizationEndDate", "authorizedHoursMonthly",
            "authorizedHoursWeekly", "assessmentType", "healthCareCertStatus",
            "healthCareCertDueDate", "mediCalAidCode", "fundingSource", "shareOfCost",
            "waiverProgram", "denialReason", "terminationReason"
        ));

        // State admins can see all fields
        permissions.put("fields.state-admin", Arrays.asList("*"));

        return permissions;
    }

    private Map<String, List<String>> getCaseNotesFieldPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();

        permissions.put("fields.basic", Arrays.asList(
            "id", "caseId", "noteType", "subject", "createdDate", "createdBy"
        ));

        // Full content requires higher access
        permissions.put("fields.case-viewer", Arrays.asList(
            "id", "caseId", "noteType", "subject", "content", "createdDate", "createdBy",
            "updatedDate", "updatedBy"
        ));

        return permissions;
    }

    private Map<String, List<String>> getRecipientFieldPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();

        // Basic fields - no PII
        permissions.put("fields.basic", Arrays.asList(
            "id", "personType", "countyCode", "firstName", "lastName"
        ));

        // Recipient viewers can see more
        permissions.put("fields.recipient-viewer", Arrays.asList(
            "id", "personType", "countyCode", "firstName", "lastName", "middleName",
            "dateOfBirth", "gender", "cin", "residenceCity", "residenceState",
            "primaryLanguage", "interpreterNeeded"
        ));

        // SSN access requires elevated permissions
        permissions.put("fields.recipient-editor", Arrays.asList(
            "id", "personType", "countyCode", "firstName", "lastName", "middleName",
            "dateOfBirth", "gender", "cin", "ssn", "ssnVerificationStatus",
            "residenceAddress", "residenceCity", "residenceState", "residenceZip",
            "mailingAddress", "mailingCity", "mailingState", "mailingZip",
            "phoneNumber", "alternatePhone", "email", "primaryLanguage",
            "interpreterNeeded", "espRegistered"
        ));

        // State admins can see all
        permissions.put("fields.state-admin", Arrays.asList("*"));

        return permissions;
    }

    private Map<String, List<String>> getProviderFieldPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();

        // Basic provider info
        permissions.put("fields.basic", Arrays.asList(
            "id", "providerNumber", "firstName", "lastName", "providerStatus",
            "dojCountyCode", "eligible"
        ));

        // Provider viewers can see more
        permissions.put("fields.provider-viewer", Arrays.asList(
            "id", "providerNumber", "firstName", "lastName", "middleName",
            "providerStatus", "dojCountyCode", "eligible", "orientationCompleted",
            "enrollmentDate", "terminationDate"
        ));

        // Provider editors can see sensitive fields
        permissions.put("fields.provider-editor", Arrays.asList(
            "id", "providerNumber", "firstName", "lastName", "middleName",
            "ssn", "ssnVerificationStatus", "dateOfBirth", "providerStatus",
            "dojCountyCode", "eligible", "ineligibleReason", "soc426Signed",
            "orientationCompleted", "soc846Signed", "workweekAgreementSigned",
            "enrollmentDate", "terminationDate", "sickLeaveEligible",
            "sickLeaveHoursAccrued", "lastSickLeaveAccrualDate"
        ));

        return permissions;
    }

    private Map<String, List<String>> getCoriFieldPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();

        // Basic CORI info - no criminal details
        permissions.put("fields.basic", Arrays.asList(
            "id", "providerId", "coriStatus", "coriTier", "coriExpirationDate"
        ));

        // CORI viewers can see more
        permissions.put("fields.cori-viewer", Arrays.asList(
            "id", "providerId", "coriStatus", "coriTier", "coriSubmissionDate",
            "coriExpirationDate", "hasRecipientWaiver"
        ));

        // CORI managers can see all including exception details
        permissions.put("fields.cori-manager", Arrays.asList(
            "id", "providerId", "coriStatus", "coriTier", "coriSubmissionDate",
            "coriExpirationDate", "coriClearanceDate", "coriDenialDate",
            "coriDenialReason", "hasRecipientWaiver", "recipientWaiverId",
            "recipientWaiverEndDate", "hasGeneralException", "generalExceptionBeginDate",
            "generalExceptionEndDate", "generalExceptionNotes"
        ));

        return permissions;
    }

    private Map<String, List<String>> getEligibilityFieldPermissions() {
        Map<String, List<String>> permissions = new HashMap<>();

        // Basic eligibility info
        permissions.put("fields.basic", Arrays.asList(
            "id", "caseId", "assessmentType", "assessmentStatus", "totalAssessedNeed"
        ));

        // Eligibility viewers can see service hours
        permissions.put("fields.eligibility-viewer", Arrays.asList(
            "id", "caseId", "assessmentType", "assessmentStatus", "totalAssessedNeed",
            "domesticHours", "personalCareHours", "paramedicalHours",
            "protectiveSupervisionHours", "transportationHours"
        ));

        // Eligibility assessors can see all assessment details
        permissions.put("fields.eligibility-assessor", Arrays.asList(
            "id", "caseId", "assessmentType", "assessmentStatus", "assessmentDate",
            "homeVisitDate", "reassessmentDueDate", "totalAssessedNeed",
            "domesticHours", "domesticHtgIndicator", "personalCareHours",
            "personalCareHtgIndicator", "paramedicalHours", "paramedicalHtgIndicator",
            "protectiveSupervisionHours", "protectiveSupervisionHtgIndicator",
            "transportationHours", "transportationHtgIndicator", "mealPrepHours",
            "relatedServicesHours", "teachingDemoHours", "mobilityRank",
            "houseworkRank", "mealPrepRank", "eatingRank", "bathingRank",
            "dressingRank", "bowelBladderRank", "mentalFunctionRank",
            "memoryRank", "orientationRank", "judgmentRank", "shareOfCostAmount",
            "shareOfCostMet", "waiverProgram", "advancePayEligible"
        ));

        return permissions;
    }

    private String getAdminToken() {
        try {
            org.springframework.util.MultiValueMap<String, String> requestBody =
                new org.springframework.util.LinkedMultiValueMap<>();
            requestBody.add("grant_type", "client_credentials");
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<org.springframework.util.MultiValueMap<String, String>> entity =
                new HttpEntity<>(requestBody, headers);

            String tokenUrl = keycloakServerUrl + "realms/" + realm + "/protocol/openid-connect/token";
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }

            throw new RuntimeException("Failed to get admin token");

        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate with Keycloak", e);
        }
    }

    private String getBackendClientInternalId(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = keycloakServerUrl + "admin/realms/" + realm + "/clients?clientId=cmips-backend";

            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> client = (Map<String, Object>) response.getBody().get(0);
                return (String) client.get("id");
            }

            throw new RuntimeException("Client not found: cmips-backend");

        } catch (Exception e) {
            throw new RuntimeException("Failed to get backend client ID", e);
        }
    }
}
