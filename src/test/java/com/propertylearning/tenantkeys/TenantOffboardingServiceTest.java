package com.propertylearning.tenantkeys;

import java.util.ArrayList;
import java.util.List;

/** Focused executable test for the offboarding order. */
public final class TenantOffboardingServiceTest {
    private TenantOffboardingServiceTest() {
    }

    public static void main(String[] args) throws Exception {
        RecordingControlPlane boundary = new RecordingControlPlane();
        new TenantOffboardingService(boundary).offboard(
                new IssuedTenantCredential("harbor-house", "user-8", "key-14", "issued-once"));
        List<String> expected = List.of("revoke:key-14", "delete:user-8");
        if (!expected.equals(boundary.actions)) {
            throw new AssertionError("Expected " + expected + " but got " + boundary.actions);
        }
        System.out.println("Offboarding order verified for harbor-house.");
    }

    private static final class RecordingControlPlane implements TenantControlPlane {
        private final List<String> actions = new ArrayList<>();

        @Override
        public String createUser(String email, String name, String tenantId, String idempotencyKey) {
            throw new AssertionError("not used by offboarding");
        }

        @Override
        public IssuedKey createKey(String tenantId, String name, List<String> scopes, String idempotencyKey) {
            throw new AssertionError("not used by offboarding");
        }

        @Override
        public void revokeKey(String keyId) {
            actions.add("revoke:" + keyId);
        }

        @Override
        public void deleteUser(String userId) {
            actions.add("delete:" + userId);
        }
    }
}
