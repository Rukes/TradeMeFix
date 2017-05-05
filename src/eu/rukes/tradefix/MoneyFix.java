package eu.rukes.tradefix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.Zrips.TradeMe.TradeMe;
import me.Zrips.TradeMe.Containers.Amounts;
import me.Zrips.TradeMe.Containers.OfferButtons;
import me.Zrips.TradeMe.Containers.TradeInfo;
import me.Zrips.TradeMe.Containers.TradeMap;
import me.Zrips.TradeMe.Containers.TradeModeInterface;
import me.Zrips.TradeMe.Containers.TradeResults;
import me.Zrips.TradeMe.Containers.TradeSize;

public class MoneyFix implements TradeModeInterface {

    private String at = "MoneyFix";
    private TradeMe plugin;

    List<ItemStack> AmountButtons = new ArrayList<ItemStack>();
    ItemStack OfferedTradeButton = new ItemStack(Material.GOLD_BLOCK);
    OfferButtons offerButton = new OfferButtons();
    Amounts amounts = new Amounts(10, 100, 1000, 10000);

    public MoneyFix(TradeMe plugin, String name) {
        this.plugin = plugin;
        at = name;
    }

    /*
     * All locale lines used for this trade mode
     */
    @Override
    public HashMap<String, Object> getLocale() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("Button.Name", "&2Zvýšit nabídku o &6[amount]");
        map.put("Button.Lore",
                Arrays.asList("&eKlikni &6levým &epro &6zvýšení &ehodnoty",
                        "&eKlikni &6pravých &epro &6snížení &ehodnoty",
                        "&eDrž &6SHIFT &epro znásobení &6x10",
                        "&eÚčet &o(ty máš)&e: &6[balance]&e$",
                        "&eAktuální peněžní nabídka: &6[offer] [taxes]",
                        "&ePo dokončení ti zůstane: &6[sourcekredit]&e$"));
        map.put("ToggleButton.Name", "&2Zahájit peněžní nabídku");
        map.put("ToggleButton.Lore", Arrays.asList(
                "&eAktuální peněžní nabídka: &6[amount] [taxes]"));
        map.put("OfferedButton.Name", "&aPeněžní nabídka &ahráče &2[player]");
        map.put("OfferedButton.Lore", Arrays.asList(
                "&eAktuální peněžní nabídka: &6[amount] [taxes]"));
        map.put("Error", "&c[playername] nemá dostatek peněz!");
        map.put("Limit", "&cChyba, nemáš dostatek peněz! Nastavena maximální hodnota [amount].");
        map.put("ChangedOffer", "&eHráč &6[playername] &ezměnil peněžní nabídku na &6[amount]&e.");
        map.put("Got", "&eObdržel jsi &6[amount]&e$");
        map.put("log", "&e[amount]&7$");
        return map;
    }

    /*
     * Used to set amounts for this trade mode
     * Changed amounts are grabbed from TradeMe config file
     */
    @Override
    public void setAmounts(Amounts amounts) {
        this.amounts = amounts;
    }

    /*
     * Used to set 4 default amount buttons when changing offer amount
     * Changed values are grabbed from TradeMe config file
     */
    @Override
    public List<ItemStack> getAmountButtons() {
        AmountButtons.add(new ItemStack(Material.GOLD_NUGGET, 1, (byte) 0));
        AmountButtons.add(new ItemStack(Material.GOLD_INGOT, 1, (byte) 0));
        AmountButtons.add(new ItemStack(Material.GOLD_BLOCK, 1, (byte) 0));
        AmountButtons.add(new ItemStack(Material.DIAMOND_BLOCK, 1, (byte) 0));
        return AmountButtons;
    }

    /*
     * Used to set default item for other player when trade offered
     * Changed value are grabbed from TradeMe config file
     */
    @Override
    public ItemStack getOfferedTradeButton() {
        OfferedTradeButton = new ItemStack(Material.EYE_OF_ENDER, 1, (byte) 0);
        return OfferedTradeButton;
    }

    /*
     * Used to set default items when particular offer exist or not
     * Changed value are grabbed from TradeMe config file
     */
    @Override
    public OfferButtons getOfferButtons() {
        offerButton.addOfferOff(new ItemStack(Material.ENDER_PEARL, 1, (byte) 0));
        offerButton.addOfferOn(new ItemStack(Material.EYE_OF_ENDER, 1, (byte) 0));
        return offerButton;
    }

    /*
     * Used to particular button in selection row on particular place
     * If not set then player will not have option to trade with that trade mode
     */
    @Override
    public void setTrade(TradeInfo trade, int i) {
        trade.getButtonList().add(trade.getPosibleButtons().get(i));
    }

    /*
     * Main buttons update event when player changes trade mode or changes trade value
     */
    @Override
    public Inventory Buttons(TradeInfo trade, Inventory GuiInv, int slot) {

        double offerAmount = trade.getOffer(at);
        ItemStack ob = offerAmount == 0 ? offerButton.getOfferOff() : offerButton.getOfferOn();

        double sourceKredit = Register.getInstance().getEconomy().getBalance(trade.getP1());
        double sourceLeft = sourceKredit - offerAmount;
        double targetKredit = Register.getInstance().getEconomy().getBalance(trade.getP2());
        double targetLeft = targetKredit + offerAmount;

        String taxes = plugin.getUtil().GetTaxesString(at, offerAmount);

        String mid = "";
        if (trade.getButtonList().size() > 4)
            mid = "\n" + plugin.getMessage("MiddleMouse");

        if (trade.Size == TradeSize.REGULAR)
            GuiInv.setItem(slot, plugin.getUtil().makeSlotItem(ob, plugin.getMessage(at, "ToggleButton.Name"),
                    plugin.getMessageListAsString(at, "ToggleButton.Lore",
                            "[amount]", offerAmount,
                            "[taxes]", taxes,
                            "[sourcekredit]", sourceLeft,
                            "[targetkredit]", targetLeft,
                            "[targetplayer]", trade.getP2().getName()) + mid));
        if (!trade.getAction().equalsIgnoreCase(at)) {
            return GuiInv;
        }

        for (int i = 45; i < 49; i++) {
            GuiInv.setItem(i, plugin.getUtil().makeSlotItem(AmountButtons.get(i - 45),
                    plugin.getMessage(at, "Button.Name",
                            "[amount]", plugin.getUtil().TrA(amounts.get(i - 45))),
                    plugin.getMessageListAsString(at, "Button.Lore",
                            "[balance]", plugin.getUtil().TrA(Register.getInstance().getEconomy().getBalance(trade.getP1())),
                            "[offer]", plugin.getUtil().TrA(offerAmount),
                            "[taxes]", taxes,
                            "[sourcekredit]", sourceLeft,
                            "[targetkredit]", targetLeft,
                            "[targetplayer]", trade.getP2().getName())));
        }
        return GuiInv;
    }

    /*
     * Event when player click on one of 4 buttons to change trade amount
     * Slot is value from 0 to 3
     */
    @Override
    public void Change(TradeInfo trade, int slot, ClickType button) {
        int amount = amounts.get(slot);
        double playerBalance = Register.getInstance().getEconomy().getBalance(trade.getP1());
        double offerAmount = trade.getOffer(at);

        if (button.isShiftClick()) {
            amount *= 10;
        }

        if (button.isLeftClick()) {
            if (offerAmount + amount > playerBalance) {
                trade.setOffer(at, playerBalance);
                trade.getP1().sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Limit", "[amount]", offerAmount));
            } else {
                trade.addOffer(at, amount);
            }
        }
        if (button.isRightClick()) {
            if (offerAmount - amount < 0) {
                trade.setOffer(at, 0);
            } else {
                trade.takeFromOffer(at, amount);
            }
        }

        String msg = plugin.getMessage(at, "ChangedOffer", "[playername]", trade.getP1().getName(), "[amount]", offerAmount);
        plugin.getAb().send(trade.getP2(), msg);
    }

    /*
     * Defines item for another player if trade offer changes
     */
    @Override
    public ItemStack getOfferedItem(TradeInfo trade) {
        if (trade.getOffer(at) <= 0) {
            return null;
        }

        double sourceLeft = Register.getInstance().getEconomy().getBalance(trade.getP1()) - trade.getOffer(at);
        double targetLeft = Register.getInstance().getEconomy().getBalance(trade.getP2()) + trade.getOffer(at);
        String taxes = plugin.getUtil().GetTaxesString(at, trade.getOffer(at));

        ItemStack item = plugin.getUtil().makeSlotItem(OfferedTradeButton, plugin.getMessage(at, "OfferedButton.Name",
                "[player]", trade.getP1Name()),
                plugin.getMessageListAsString(at, "OfferedButton.Lore",
                        "[player]", trade.getP1().getName(),
                        "[amount]", trade.getOffer(at),
                        "[taxes]", taxes,
                        "[sourcekredit]", sourceLeft < 0 ? 0 : sourceLeft,
                        "[targetkredit]", targetLeft,
                        "[targetplayer]", trade.getP2().getName()));
        return item;
    }

    /*
     * Check performed when both players accepts trade
     * Extra checks can be performed to double check if trade can be finalized
     */
    @Override
    public boolean isLegit(TradeMap trade) {
        Player p1 = trade.getP1Trade().getP1();
        Player p2 = trade.getP2Trade().getP1();
        if (Register.getInstance().getEconomy().getBalance(p1) < trade.getP1Trade().getOffer(at)) {
            p1.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
            p2.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Error", "[playername]", p1.getName()));
            return false;
        }
        if (Register.getInstance().getEconomy().getBalance(p2) < trade.getP2Trade().getOffer(at)) {
            p1.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
            p2.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Error", "[playername]", p2.getName()));
            return false;
        }
        return true;
    }

    /*
     * Final action for this trade mode after trade is finally gets green light
     */
    @Override
    public boolean finish(TradeInfo trade) {
        Player target = trade.getP2();
        Player source = trade.getP1();
        if (trade.getOffer(at) <= 0) {
            return false;
        }

        double amount = trade.getOffer(at);
        if(!Register.getInstance().getEconomy().has(source.getName(), amount)){
            return false;
        }
        withdraw(source, amount);
        /*

        Here I found the issue, with taxes so it's disabled now

         */
        //amount = plugin.getUtil().CheckTaxes(at, amount);

        //trade.setOffer(at, amount);
        deposite(target, amount);
        if (target != null) {
            target.sendMessage(plugin.getMessage("Prefix") + plugin.getMessage(at, "Got", "[amount]", trade.getOffer(at)));
        }
        return true;
    }

    /*
     * Returns results of this trade mode
     */
    @Override
    public void getResults(TradeInfo trade, TradeResults TR) {
        if (trade.getOffer(at) <= 0) {
            return;
        }
        double amount = plugin.getUtil().CheckTaxes(at, trade.getOffer(at));
        TR.add(at, amount);
    }

    /*
     * This event will be fired when player click second time on same trade mode
     * Can be used to switch in example between skills for McMMO exp trade
     * Should return new name of changed skill value
     */
    @Override
    public String Switch(TradeInfo trade, ClickType button) {
        return null;
    }

    private void withdraw(Player player, double exp) {
        if (player == null) {
            return;
        }
        Register.getInstance().getEconomy().withdrawPlayer(player.getName(), exp);
    }

    private void deposite(Player player, double exp) {
        if (player == null) {
            return;
        }
        Register.getInstance().getEconomy().depositPlayer(player.getName(), exp);
    }
}