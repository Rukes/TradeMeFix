package eu.rukes.tradefix;

import me.Zrips.TradeMe.Containers.AmountClickAction;
import me.Zrips.TradeMe.Containers.TradeAction;
import me.Zrips.TradeMe.TradeMe;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Rukes on 5.5.2017.
 */
public class Register extends JavaPlugin{

    private static Register instance;
    private Economy economy;

    @Override
    public void onEnable(){
        instance = this;
        if(setupEconomy()){
            TradeAction tradeActionMoney = new TradeAction("MoneyFix", AmountClickAction.Amounts, false);
            TradeMe.getInstance().addNewTradeMode(tradeActionMoney, new MoneyFix(TradeMe.getInstance(), "MoneyFix"));
            TradeMe.getInstance().getCM().reload();
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[TradeMeFix] " + ChatColor.GOLD + "Injected MoneyFix trade mode!");
        }else{
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[TradeMeFix] Couldn't setup economy as well!");
        }
    }
    @Override
    public void onDisable(){}

    public static Register getInstance() {
        return instance;
    }
    public Economy getEconomy() {
        return economy;
    }

    private boolean setupEconomy(){
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
}
