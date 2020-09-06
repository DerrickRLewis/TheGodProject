package apps.envision.mychurch.utils;

import com.google.android.gms.wallet.WalletConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class Constants {

    public static String APP_DATABASE = "theGodProjectDb";//app database name
    public static final String BASE_URL = "http://thegodproject.xyz/"; //remote api url
    public static final String BIBLE_DOWNLOAD_URL = "http://thegodproject.xyz/uploads/"; //remote api url
    public static String Terms_URL = "http://thegodproject.xyz/terms"; //app terms url
    public static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);     // 1 day to milliseconds
    public static final long FIFTEEN_MINUTES = TimeUnit.MINUTES.toMillis(15);     // 15 minutes to milliseconds

    public interface STRIPE{
        int PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_PRODUCTION; //change to WalletConstants.ENVIRONMENT_PRODUCTION when you are ready to publish your app
    }

    /**
     * Subscription Billing plans
     */
    public interface BILLING {
        String ONE_WEEK_SUB = "one_week_sub";
        String ONE_MONTH_SUB = "one_month_sub";
        String THREE_MONTHS_SUB = "3_months_sub";
        String SIX_MONTHS_SUB = "6_months_sub";
        String ONE_YEAR_SUB = "1_year_sub";
    }

    public interface Uploads{
        int maximum_total_file_size = 20; //Total maximum files users can upload in MB
        int maximum_single_file_size = 10; //single file size users can upload in MB
    }

    public interface BIBLE_DOWNLOAD_IDENTFIER {
        String AMP_IDENTIFIER = "AMP_IDENTIFIER";
        String KJV_IDENTIFIER = "KJV_IDENTIFIER";
        String NKJV_IDENTIFIER = "NKJV_IDENTIFIER";
        String NLT_IDENTIFIER = "NLT_IDENTIFIER";
        String MSG_IDENTIFIER = "MSG_IDENTIFIER";
        String NRSV_IDENTIFIER = "NRSV_IDENTIFIER";
        String NIV_IDENTIFIER = "NIV_IDENTIFIER";
    }

    public interface BIBLE_VERSIONS {
        String AMP = "AMP";
        String KJV = "KJV";
        String NKJV = "NKJV";
        String NLT = "NLT";
        String MSG = "MSG";
        String NRSV = "NRSV";
        String NIV = "NIV";
    }


    public interface GOOGLE_SEARCH{
        String SEARCH_API = "http://suggestqueries.google.com/complete/search?q=%1$s&client=firefox&hl=fr";
    }

    //interface for common app settings strings
    public interface SETTINGS{
        String SEARCH_OFFSET = "search_offset";
        String SEARCH_QUERY = "search_query";
        String AUDIO_PLAYER_REPEAT_MODE = "audio_player_repeat_mode";
    }

    public interface NOTIFICATION {
        String CHANNEL_ID = "APP_10001";
    }




    /**
     * Changing this to ENVIRONMENT_PRODUCTION will make the API return chargeable card information.
     * Please refer to the documentation to read about the required steps needed to enable
     * ENVIRONMENT_PRODUCTION.
     *
     * @value #PAYMENTS_ENVIRONMENT
     */
    public static final int PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;

    /**
     * The allowed networks to be requested from the API. If the user has cards from networks not
     * specified here in their account, these will not be offered for them to choose in the popup.
     *
     * @value #SUPPORTED_NETWORKS
     */
    public static final List<String> SUPPORTED_NETWORKS = Arrays.asList(
            "AMEX",
            "DISCOVER",
            "JCB",
            "MASTERCARD",
            "VISA");

    /**
     * The Google Pay API may return cards on file on Google.com (PAN_ONLY) and/or a device token on
     * an Android device authenticated with a 3-D Secure cryptogram (CRYPTOGRAM_3DS).
     *
     * @value #SUPPORTED_METHODS
     */
    public static final List<String> SUPPORTED_METHODS =
            Arrays.asList(
                    "PAN_ONLY",
                    "CRYPTOGRAM_3DS");

    /**
     * Required by the API, but not visible to the user.
     *
     * @value #COUNTRY_CODE Your local country
     */
    public static final String COUNTRY_CODE = "US";

    /**
     * Required by the API, but not visible to the user.
     *
     * @value #CURRENCY_CODE Your local currency
     */
    public static final String CURRENCY_CODE = "USD";

    /**
     * Supported countries for shipping (use ISO 3166-1 alpha-2 country codes). Relevant only when
     * requesting a shipping address.
     *
     * @value #SHIPPING_SUPPORTED_COUNTRIES
     */
    public static final List<String> SHIPPING_SUPPORTED_COUNTRIES = Arrays.asList("US", "GB");

    /**
     * The name of your payment processor/gateway. Please refer to their documentation for more
     * information.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_NAME
     */
    public static final String PAYMENT_GATEWAY_TOKENIZATION_NAME = "example";

    /**
     * Custom parameters required by the processor/gateway.
     * In many cases, your processor / gateway will only require a gatewayMerchantId.
     * Please refer to your processor's documentation for more information. The number of parameters
     * required and their names vary depending on the processor.
     *
     * @value #PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS
     */
    public static final HashMap<String, String> PAYMENT_GATEWAY_TOKENIZATION_PARAMETERS =
            new HashMap<String, String>() {
                {
                    put("gateway", PAYMENT_GATEWAY_TOKENIZATION_NAME);
                    put("gatewayMerchantId", "exampleGatewayMerchantId");
                    // Your processor may require additional parameters.
                }
            };

    /**
     * Only used for {@code DIRECT} tokenization. Can be removed when using {@code PAYMENT_GATEWAY}
     * tokenization.
     *
     * @value #DIRECT_TOKENIZATION_PUBLIC_KEY
     */
    public static final String DIRECT_TOKENIZATION_PUBLIC_KEY = "REPLACE_ME";

    /**
     * Parameters required for {@code DIRECT} tokenization.
     * Only used for {@code DIRECT} tokenization. Can be removed when using {@code PAYMENT_GATEWAY}
     * tokenization.
     *
     * @value #DIRECT_TOKENIZATION_PARAMETERS
     */
    public static final HashMap<String, String> DIRECT_TOKENIZATION_PARAMETERS =
            new HashMap<String, String>() {
                {
                    put("protocolVersion", "ECv2");
                    put("publicKey", DIRECT_TOKENIZATION_PUBLIC_KEY);
                }
            };


}
