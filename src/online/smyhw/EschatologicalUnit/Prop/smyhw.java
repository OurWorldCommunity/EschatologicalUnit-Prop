package online.smyhw.EschatologicalUnit.Prop;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;


public class smyhw extends JavaPlugin implements Listener {
    public static Plugin smyhw_;
    public static Logger loger;
    public static FileConfiguration configer;
    public static String prefix;
    public static HashMap<String, itemData> itemList;

    static void CSBZ(CommandSender sender) {
        sender.sendMessage(prefix + "非法使用 | 使用者信息已记录，此事将被上报");
        loger.warning(prefix + "使用者<" + sender.getName() + ">试图非法使用指令{参数不足}");
    }

    @Override
    public void onEnable() {
        getLogger().info("EschatologicalUnit.Prop加载");
        getLogger().info("正在加载环境...");
        loger = getLogger();
        configer = getConfig();
        smyhw_ = this;
        getLogger().info("正在加载配置...");
        saveDefaultConfig();
        prefix = configer.getString("config.prefix");

        Set<String> temp1 = configer.getConfigurationSection("items").getKeys(false);
        loger.info(String.valueOf(temp1));
        itemList = new HashMap<String, itemData>();
        int delay_index = 0;
        for (String temp2 : temp1) {
            itemList.put(temp2, new itemData(temp2,delay_index));
            delay_index++;
        }
        getLogger().info("正在注册监听器...");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("EschatologicalUnit.Prop加载完成");
    }

    @Override
    public void onDisable() {
        for (String tmp1 : itemList.keySet()) {
            itemList.get(tmp1).disableFlag = true;
        }
        getLogger().info("EschatologicalUnit.Prop卸载");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("euP")) {
            if (!sender.hasPermission("eu.plugin")) {
                sender.sendMessage(prefix + "非法使用 | 使用者信息已记录，此事将被上报");
                loger.warning(prefix + "使用者<" + sender.getName() + ">试图非法使用指令<" + args + ">{权限不足}");
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage(prefix + "非法使用 | 使用者信息已记录，此事将被上报");
                loger.warning(prefix + "使用者<" + sender.getName() + ">试图非法使用指令<" + args + ">{参数不足}");
                return true;
            }
            switch (args[0]) {
                case "reload": {
                    reloadConfig();
                    this.onDisable();
                    sender.sendMessage(prefix + "重载配置文件...");
                    this.onEnable();
                    return true;
                }
                case "set": {
                    if (args.length < 2) {
                        CSBZ(sender);
                        return true;
                    }
                    Player p = (Player) sender;
                    ItemStack is = p.getInventory().getItemInMainHand();
                    configer.set("items." + args[1] + ".data", is);
                    configer.set("items." + args[1] + ".Loc.only_world", true);
                    configer.set("items." + args[1] + ".Loc.world", "our_world");
                    configer.set("items." + args[1] + ".Loc.x", 0);
                    configer.set("items." + args[1] + ".Loc.y", 0);
                    configer.set("items." + args[1] + ".Loc.z", 0);
                    configer.set("items." + args[1] + ".anyLoc", true);
                    configer.set("items." + args[1] + ".anyItem", false);
                    configer.set("items." + args[1] + ".time", 60);
                    configer.set("items." + args[1] + ".msg", "剩余时间");
                    configer.set("items." + args[1] + ".consume", false);
                    configer.set("items." + args[1] + ".cycle_time", 10);
                    configer.set("items." + args[1] + ".cmd", new ArrayList());
                    saveConfig();
                    sender.sendMessage(prefix + "设置成功，您可能需要重载服务器");
                    return true;
                }
                case "get": {
                    if (args.length < 2) {
                        CSBZ(sender);
                        return true;
                    }
                    ItemStack data = smyhw.configer.getItemStack("items." + args[1] + ".data");
                    sender.sendMessage(data.toString());
                    ((Player) sender).getInventory().setItemInMainHand(data);
                    return true;
                }
                case "disable": {//禁用某个实例
                    if (args.length < 2) {
                        CSBZ(sender);
                        return true;
                    }
                    itemData temp1 = itemList.get(args[1]);
                    if (temp1 == null) {
                        sender.sendMessage(prefix + "实例<" + args[1] + ">不存在");
                        return true;
                    }
                    temp1.activation = false;
                    sender.sendMessage(prefix + "实例<" + args[1] + ">已禁用");
                    return true;
                }
                case "enable": {//启用某个实例
                    if (args.length < 2) {
                        CSBZ(sender);
                        return true;
                    }
                    itemData temp1 = itemList.get(args[1]);
                    if (temp1 == null) {
                        sender.sendMessage(prefix + "实例<" + args[1] + ">不存在");
                        return true;
                    }
                    temp1.activation = true;
                    sender.sendMessage(prefix + "实例<" + args[1] + ">已启用");
                    return true;
                }
                default:
                    CSBZ(sender);
            }
            return true;
        }
        return false;
    }

}



