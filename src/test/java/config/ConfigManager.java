package config;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigManager {
    private static Properties prop1= new Properties();
    private static Properties prop2 = new Properties();
    static {
        loadBaseConfigProperties();
        loadEnvironmentSpecificProperties(getEnvironment());
    }

    public static void loadBaseConfigProperties()
        {
            try{

                FileInputStream baseConfigfile= new FileInputStream("src/test/resources/config.properties");
                prop1.load(baseConfigfile);

            }
            catch(Exception e)
            {
                e.printStackTrace();

            }
        }

    public static void loadEnvironmentSpecificProperties( String environment)
    {
        try{

            FileInputStream envSpecificFile= new FileInputStream("src/test/resources/config-"+environment.toLowerCase()+".properties");
            prop2.load(envSpecificFile);

        }
        catch(Exception e)
        {
            e.printStackTrace();

        }
    }

    public static String getEnvironment()
    {
        return prop1.getProperty("environment");
    }
    public static String getDigilockerBaserUrl()
    {
        return prop1.getProperty("digilockerMockService.baseUri");
    }
    public static String getpaymentMockUrl()
    {
        return prop1.getProperty("paymentMockService.baseUri");
    }
    public static String getJioSignMockUrl()
    {
        return prop1.getProperty("jioSignMockService.baseUri");
    }
    public static String getBaseUri()
    {
        return prop2.getProperty("baseUri");
    }
    public static String getUserId()
    {
        return prop2.getProperty("userId");
    }
    public static String getPlatformID()
    {
        return prop2.getProperty("platformID");
    }
    public static String getCompanyID()
    {
        return prop2.getProperty("companyID");
    }

}
