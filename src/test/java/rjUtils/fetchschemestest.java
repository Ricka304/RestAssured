package rjUtils;

import config.ConfigManager;
import config.Constants;
import endpoints.GSSEndpoints;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.path.json.JsonPath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class fetchschemestest {

    public static String FILE_PATH = "src/test/resources/testdata/Migrated_Active_Schemes_v1.4.xlsx";

    public static String normalizeIndianMobile(String input) {
        if (input == null) return null;
        String digits = input.replaceAll("\\D", "");
        if (digits.length() <= 10) return digits;
        return digits.substring(digits.length() - 10);
    }

    /**
     * Fetch schemes for a phone and return monthsOfInstallmentPaid for given schemeId.
     * Returns "0" when not found, null on unexpected error.
     */
    public static String fetchGssSchemes(String phoneNumber, String schemeId) {
        try {
            io.restassured.response.Response resp = given()
                    .relaxedHTTPSValidation()
                    .baseUri(ConfigManager.getBaseUri())
                    .pathParam("param1", GSSEndpoints.EXTENSION_PATH_PARAM)
                    .pathParam("param2", GSSEndpoints.FETCH_DASHBOARD_SCHEMES)
                    .pathParam("param3", phoneNumber)
                    .header("Content-Type", Constants.CONTENT_TYPE_JSON)
                    .header("UserId", ConfigManager.getUserId())
                    .when()
                    .get("{param1}{param2}/{param3}");

            int status = resp.getStatusCode();
            String body = resp.getBody() == null ? "" : resp.getBody().asString();
            String ct = resp.getHeader("Content-Type");

            System.out.printf("fetchGssSchemes: phone=%s scheme=%s status=%d contentType=%s bodyLen=%d%n",
                    phoneNumber, schemeId, status, ct, body == null ? 0 : body.length());

            if (status < 200 || status >= 300) {
                System.err.printf("Non-2xx response for phone=%s: status=%d%nBody preview:%n%s%n",
                        phoneNumber, status, preview(body));
                return null;
            }

            if (body == null || body.trim().isEmpty()) {
                System.err.printf("Empty response body for phone=%s%n", phoneNumber);
                return "0";
            }

            if (!ct.toLowerCase().contains("application/json") && !looksLikeJson(body)) {
                saveDebugResponse(phoneNumber, body);
                System.err.printf("Non-JSON response for phone=%s; Content-Type=%s; saved debug copy.%n", phoneNumber, ct);
                return null;
            }

            JsonPath jp;
            try {
                jp = new JsonPath(body);
            } catch (Exception e) {
                System.err.printf("Failed to parse JSON for phone=%s: %s%nBody preview:%n%s%n",
                        phoneNumber, e.getMessage(), preview(body));
                saveDebugResponse(phoneNumber, body);
                return null;
            }

            List<Object> ongoing = jp.getList("ongoingSchemes");
            if (ongoing == null || ongoing.isEmpty()) return "0";

            for (int i = 0; i < ongoing.size(); i++) {
                Map<String, Object> schemeMap = jp.getMap("ongoingSchemes[" + i + "]");
                Object sid = schemeMap.get("schemeId");
                if (sid != null && sid.toString().equals(schemeId)) {
                    Object monthsObj = schemeMap.get("monthsOfInstallmentPaid");
                    return monthsObj == null ? "0" : monthsObj.toString();
                }
            }
            return "0";
        } catch (Exception e) {
            System.err.printf("fetchGssSchemes error for phone=%s scheme=%s -> %s%n", phoneNumber, schemeId, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /* --- helper functions --- */
    private static boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.startsWith("{") || t.startsWith("[") || t.startsWith("\"");
    }

    private static String preview(String body) {
        if (body == null) return "";
        return body.length() <= 1000 ? body : body.substring(0, 1000) + "...(truncated)";
    }

    private static void saveDebugResponse(String phone, String body) {
        try {
            String safePhone = phone == null ? "null" : phone.replaceAll("[^0-9A-Za-z_-]", "_");
            java.nio.file.Path p = java.nio.file.Paths.get("debug_responses", "resp_" + safePhone + "_" + System.currentTimeMillis() + ".txt");
            java.nio.file.Files.createDirectories(p.getParent());
            java.nio.file.Files.writeString(p, body, java.nio.file.StandardOpenOption.CREATE_NEW);
        } catch (Exception ignore) {
            // don't let debug saving break the flow
        }
    }

    /**
     * Example main that uses the streaming processor.
     */
    public static void main(String[] args) throws Exception {
        // default cookie (only if you need it)
        io.restassured.RestAssured.requestSpecification = new RequestSpecBuilder()
                .addCookie("f.session", "s%3ADty1u5u5DjpG-v0NH4EXRQnskbNMNxtf.dNM8uOC06vtODUs1KBtR%2B6o4VvwRf1ALeCOcDKeH2kk")
                .build();

        Path input = Paths.get(FILE_PATH).toAbsolutePath().normalize();
        if (!Files.exists(input)) throw new RuntimeException("Input not found: " + input);
        String inputPath = input.toString();
        String outputPath = inputPath.replace(".xlsx", "_output_streamed.xlsx");
        Files.copy(input, Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);

        // configure processor
        utils.StreamingBulkExcelProcessor.Config cfg = new utils.StreamingBulkExcelProcessor.Config(
                inputPath,
                "Sheet1",
                outputPath,
                "Sheet1",
                List.of("InstalmentPaid_PO"),
                4,   // THREAD_COUNT
                100  // QUEUE_CAPACITY
        );

        // column headers in input
        final String phoneHeader = "CustMobile";
        final String schemeHeader = "GSSAccountNo";

        utils.StreamingBulkExcelProcessor.RowHandler handler = (rowMap, rowIndex) -> {
            String phone = rowMap.getOrDefault(phoneHeader, "").trim();
            String schemeId = rowMap.getOrDefault(schemeHeader, "").trim();
            String normalized = normalizeIndianMobile(phone);
            String months = fetchGssSchemes(normalized, schemeId);
            if (months == null) months = "0";
            return Map.of("InstalmentPaid_PO", months);
        };

        utils.StreamingBulkExcelProcessor.process(cfg, handler);
    }
}
