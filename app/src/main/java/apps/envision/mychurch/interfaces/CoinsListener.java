package apps.envision.mychurch.interfaces;

import apps.envision.mychurch.pojo.Coins;

/**
 * Created by Ray on 6/25/2018.
 */

public interface CoinsListener {
    void purchaseCoins(Coins coins);
    void onViewPurchaseHistory();
}
