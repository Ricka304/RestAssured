package utils;

import net.datafaker.Faker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DataGenerator - Comprehensive fake and random data generation
 * 
 * Features:
 * - Personal information (names, emails, phones)
 * - Address and location data
 * - Financial data (credit cards, amounts)
 * - Date and time generation
 * - Random numbers and strings
 * - API-specific test data
 * - Bulk data generation
 */
public class DataGenerator {
    
    private static final Faker faker = new Faker();
    private static final Random random = new Random();
    
    // ====================
    // PERSONAL DATA
    // ====================
    
    /**
     * Generate random first name
     */
    public static String generateFirstName() {
        return faker.name().firstName();
    }
    
    /**
     * Generate random last name
     */
    public static String generateLastName() {
        return faker.name().lastName();
    }
    
    /**
     * Generate random full name
     */
    public static String generateFullName() {
        return faker.name().fullName();
    }
    
    /**
     * Generate random email address
     */
    public static String generateEmail() {
        return faker.internet().emailAddress();
    }
    
    /**
     * Generate email with specific domain
     */
    public static String generateEmail(String domain) {
        String username = faker.internet().username();
        return username + "@" + domain;
    }
    
    /**
     * Generate random phone number
     */
    public static String generatePhoneNumber() {
        return faker.phoneNumber().phoneNumber();
    }
    
    /**
     * Generate Indian mobile number
     */
    public static String generateIndianMobile() {
        String[] prefixes = {"70", "71", "72", "73", "74", "75", "76", "77", "78", "79", 
                           "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", 
                           "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        return prefix + String.format("%08d", random.nextInt(100000000));
    }
    
    /**
     * Generate random username
     */
    public static String generateUsername() {
        return faker.internet().username();
    }
    
    /**
     * Generate random password
     */
    public static String generatePassword() {
        return faker.internet().password(8, 16, true, true, true);
    }
    
    /**
     * Generate strong password with specific criteria
     */
    public static String generateStrongPassword(int minLength, int maxLength) {
        return faker.internet().password(minLength, maxLength, true, true, true);
    }
    
    // ====================
    // ADDRESS & LOCATION
    // ====================
    
    /**
     * Generate random address
     */
    public static String generateAddress() {
        return faker.address().streetAddress();
    }
    
    /**
     * Generate random city
     */
    public static String generateCity() {
        return faker.address().city();
    }
    
    /**
     * Generate random state
     */
    public static String generateState() {
        return faker.address().state();
    }
    
    /**
     * Generate random ZIP code
     */
    public static String generateZipCode() {
        return faker.address().zipCode();
    }
    
    /**
     * Generate Indian PIN code
     */
    public static String generateIndianPinCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * Generate random country
     */
    public static String generateCountry() {
        return faker.address().country();
    }
    
    // ====================
    // FINANCIAL DATA
    // ====================
    
    /**
     * Generate random credit card number
     */
    public static String generateCreditCardNumber() {
        return faker.finance().creditCard();
    }
    
    /**
     * Generate random amount between min and max
     */
    public static double generateAmount(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
    
    /**
     * Generate random amount with specific decimal places
     */
    public static String generateAmountString(double min, double max, int decimalPlaces) {
        double amount = generateAmount(min, max);
        return String.format("%." + decimalPlaces + "f", amount);
    }
    
    /**
     * Generate random currency code
     */
    public static String generateCurrencyCode() {
        String[] currencies = {"USD", "EUR", "GBP", "INR", "JPY", "AUD", "CAD", "CHF"};
        return currencies[random.nextInt(currencies.length)];
    }
    
    /**
     * Generate random bank account number
     */
    public static String generateBankAccountNumber() {
        return String.format("%012d", random.nextLong() % 1000000000000L);
    }
    
    /**
     * Generate random IFSC code (Indian)
     */
    public static String generateIfscCode() {
        String[] bankCodes = {"SBIN", "HDFC", "ICIC", "AXIS", "PUNB", "UBIN", "CBIN", "IOBA"};
        String bankCode = bankCodes[random.nextInt(bankCodes.length)];
        return bankCode + "0" + String.format("%06d", random.nextInt(1000000));
    }
    
    // ====================
    // DATE & TIME
    // ====================
    
    /**
     * Generate random date between two dates
     */
    public static String generateDateBetween(String startDate, String endDate, String format) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        long daysBetween = end.toEpochDay() - start.toEpochDay();
        long randomDays = ThreadLocalRandom.current().nextLong(0, daysBetween + 1);
        
        LocalDate randomDate = start.plusDays(randomDays);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return randomDate.format(formatter);
    }
    
    /**
     * Generate random future date
     */
    public static String generateFutureDate(int daysFromNow, String format) {
        LocalDate futureDate = LocalDate.now().plusDays(random.nextInt(daysFromNow) + 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return futureDate.format(formatter);
    }
    
    /**
     * Generate random past date
     */
    public static String generatePastDate(int daysAgo, String format) {
        LocalDate pastDate = LocalDate.now().minusDays(random.nextInt(daysAgo) + 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return pastDate.format(formatter);
    }
    
    /**
     * Generate current date in specific format
     */
    public static String getCurrentDate(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.now().format(formatter);
    }
    
    /**
     * Generate random timestamp
     */
    public static long generateTimestamp() {
        return System.currentTimeMillis() + random.nextInt(86400000); // Random time within next 24 hours
    }
    
    // ====================
    // NUMBERS & STRINGS
    // ====================
    
    /**
     * Generate random integer between min and max (inclusive)
     */
    public static int generateRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    
    /**
     * Generate random long between min and max (inclusive)
     */
    public static long generateRandomLong(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }
    
    /**
     * Generate random double between min and max
     */
    public static double generateRandomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
    
    /**
     * Generate random alphanumeric string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate random alphabetic string
     */
    public static String generateRandomAlphaString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate random numeric string
     */
    public static String generateRandomNumericString(int length) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate random UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generate random UUID without hyphens
     */
    public static String generateUUIDWithoutHyphens() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    // ====================
    // API-SPECIFIC DATA
    // ====================
    
    /**
     * Generate random JSON object as string
     */
    public static String generateRandomJsonObject() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("id", generateRandomInt(1000, 9999));
        jsonMap.put("name", generateFullName());
        jsonMap.put("email", generateEmail());
        jsonMap.put("phone", generatePhoneNumber());
        jsonMap.put("timestamp", generateTimestamp());
        
        return JsonUtils.toJsonString(jsonMap);
    }
    
    /**
     * Generate random user registration data
     */
    public static Map<String, String> generateUserRegistrationData() {
        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", generateFirstName());
        userData.put("lastName", generateLastName());
        userData.put("email", generateEmail());
        userData.put("phone", generateIndianMobile());
        userData.put("password", generateStrongPassword(8, 12));
        userData.put("dateOfBirth", generatePastDate(365 * 30, "yyyy-MM-dd")); // 30 years max
        userData.put("city", generateCity());
        userData.put("pinCode", generateIndianPinCode());
        
        return userData;
    }
    
    /**
     * Generate random login credentials
     */
    public static Map<String, String> generateLoginCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", generateEmail());
        credentials.put("password", generatePassword());
        
        return credentials;
    }
    
    /**
     * Generate random API key
     */
    public static String generateApiKey() {
        return "api_" + generateRandomString(32);
    }
    
    /**
     * Generate random Bearer token
     */
    public static String generateBearerToken() {
        return "Bearer " + generateRandomString(64);
    }
    
    // ====================
    // BULK DATA GENERATION
    // ====================
    
    /**
     * Generate multiple users data
     */
    public static List<Map<String, String>> generateMultipleUsers(int count) {
        List<Map<String, String>> users = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            users.add(generateUserRegistrationData());
        }
        
        return users;
    }
    
    /**
     * Generate test data for CSV format
     */
    public static List<Map<String, String>> generateTestDataForCsv(int count) {
        List<Map<String, String>> testData = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Map<String, String> data = new HashMap<>();
            data.put("TestCase", "TC_" + String.format("%03d", i + 1));
            data.put("Username", generateEmail());
            data.put("Password", generatePassword());
            data.put("FirstName", generateFirstName());
            data.put("LastName", generateLastName());
            data.put("Phone", generateIndianMobile());
            data.put("Amount", generateAmountString(100, 10000, 2));
            data.put("Expected", "Success");
            
            testData.add(data);
        }
        
        return testData;
    }
    
    // ====================
    // HELPER METHODS
    // ====================
    
    /**
     * Get random item from array
     */
    public static <T> T getRandomFromArray(T[] array) {
        return array[random.nextInt(array.length)];
    }
    
    /**
     * Get random item from list
     */
    public static <T> T getRandomFromList(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
    
    /**
     * Generate random boolean
     */
    public static boolean generateRandomBoolean() {
        return random.nextBoolean();
    }
    
    /**
     * Generate random boolean with custom probability
     */
    public static boolean generateRandomBoolean(double trueProbability) {
        return random.nextDouble() < trueProbability;
    }
    
    /**
     * Shuffle a list randomly
     */
    public static <T> List<T> shuffleList(List<T> list) {
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);
        return shuffled;
    }
}

