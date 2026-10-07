package com.propertylearning.tenantkeys;

/** Local domain context that makes the credential's tenant boundary visible. */
public record TenantLearningWorkspace(
        String tenantId,
        MaintenanceRequest maintenanceRequest,
        TenantDocument tenantDocument,
        InspectionReminder inspectionReminder) {
}

record MaintenanceRequest(String title, String status) {
}

record TenantDocument(String title, String status) {
}

record InspectionReminder(String title, String dueDate) {
}
