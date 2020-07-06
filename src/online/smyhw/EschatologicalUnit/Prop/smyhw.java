package online.smyhw.EschatologicalUnit.Prop;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class smyhw extends JavaPlugin implements Listener 
{
	public static Plugin smyhw_;
	public static Logger loger;
	public static FileConfiguration configer;
	public static String prefix;
	public static HashMap<String,ItemData> itemList;
	@Override
    public void onEnable() 
	{
		getLogger().info("EschatologicalUnit.Prop加载");
		getLogger().info("正在加载环境...");
		loger=getLogger();
		configer = getConfig();
		smyhw_=this;
		getLogger().info("正在加载配置...");
		saveDefaultConfig();
		prefix = configer.getString("config.prefix");
		
		Set<String> temp1 = configer.getConfigurationSection("items").getKeys(false);
		loger.info(temp1+"");
		itemList = new HashMap<String,ItemData>();
		for(String temp2:temp1)
		{
			itemList.put(temp2, new ItemData(temp2));
		}
		getLogger().info("正在注册监听器...");
		Bukkit.getPluginManager().registerEvents(this,this);
		getLogger().info("EschatologicalUnit.Prop加载完成");
    }

	@Override
    public void onDisable() 
	{
		getLogger().info("EschatologicalUnit.Prop卸载");
    }
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
        if (cmd.getName().equals("euP"))
        {
                if(!sender.hasPermission("eu.plugin")) 
                {
                	sender.sendMessage(prefix+"非法使用 | 使用者信息已记录，此事将被上报");
                	loger.warning(prefix+"使用者<"+sender.getName()+">试图非法使用指令<"+args+">{权限不足}");
                	return true;
                }
                if(args.length<1) 
                {
                	sender.sendMessage(prefix+"非法使用 | 使用者信息已记录，此事将被上报");
                	loger.warning(prefix+"使用者<"+sender.getName()+">试图非法使用指令<"+args+">{参数不足}");
                	return true;
                }
                switch(args[0])
                {
                case "reload":
                {
                	reloadConfig();
                	configer=getConfig();
                	sender.sendMessage(prefix+"重载配置文件...");
                	return true;
                }
                case"set":
                {
                	if(args.length<2) {CSBZ(sender);return true;}
                	Player p = (Player)sender;
                	ItemStack is = p.getInventory().getItemInMainHand();
                	Map m = is.serialize();
                	configer.set("items."+args[1]+".data", is);
                	configer.set("items."+args[1]+".Loc.x", 0);
                	configer.set("items."+args[1]+".Loc.y", 0);
                	configer.set("items."+args[1]+".Loc.z", 0);
                	configer.set("items."+args[1]+".anyLoc", true);
                	configer.set("items."+args[1]+".anyItem", false);
                	configer.set("items."+args[1]+".time", 60);
                	configer.set("items."+args[1]+".msg", "剩余时间");
                	configer.set("items."+args[1]+".cmd", new ArrayList());
                	saveConfig();
                	sender.sendMessage(prefix+"设置成功，您可能需要重载服务器");
                	return true;
                }
                case "get":
                {
                	if(args.length<2) {CSBZ(sender);return true;}
                	ItemStack data = smyhw.configer.getItemStack("items."+args[1]+".data");
                	sender.sendMessage(data.toString());
                	((Player)sender).getInventory().setItemInMainHand(data);
                	return true;
                }
                default:
                	CSBZ(sender);
                }
                return true;                                                       
        }
       return false;
	}
	
	static void CSBZ(CommandSender sender)
	{
		sender.sendMessage(prefix+"非法使用 | 使用者信息已记录，此事将被上报");
		loger.warning(prefix+"使用者<"+sender.getName()+">试图非法使用指令{参数不足}");
	}
	
}



class ItemData extends BukkitRunnable
{
	public ItemStack data;
	public int time;
	public HashMap<String,Integer> PlayerTimeMap = new HashMap<String,Integer>();
	public String ID;
	public String msg;
	public boolean anyItem;
	public boolean anyLoc;
	public int x;
	public int y;
	public int z;
	public ItemData(String ID)
	{
		this.ID=ID;
		data = smyhw.configer.getItemStack("items."+ID+".data");
		time = smyhw.configer.getInt("items."+ID+".time");
		anyItem = smyhw.configer.getBoolean("items."+ID+".anyItem");
		anyLoc = smyhw.configer.getBoolean("items."+ID+".anyLoc");
		x = smyhw.configer.getInt("items."+ID+".Loc.x");
		y = smyhw.configer.getInt("items."+ID+".Loc.y");
		z = smyhw.configer.getInt("items."+ID+".Loc.z");
		Collection<? extends Player> player_list = Bukkit.getOnlinePlayers();
		for(Player p : player_list)
		{
			PlayerTimeMap.put(p.getName(), time);
		}
		this.msg = smyhw.configer.getString("items."+ID+".msg");
		this.runTaskTimer(smyhw.smyhw_,0,10);
	}
	
	@Override
	public void run() 
	{
		Collection<? extends Player> player_list = Bukkit.getOnlinePlayers();
		for(Player p : player_list)
		{
			Location temp2 = p.getLocation();
			int px = (int) temp2.getX();
			int py = (int) temp2.getY();
			int pz = (int) temp2.getZ();
			if(!anyLoc)
			{//筛选 坐标
				if( (px < (x-1)) || (px > (x+1)) ) { reset(p.getName()); continue; }
				if( (py < (y-1)) || (py > (y+1)) ) { reset(p.getName()); continue; }
				if( (pz < (z-1)) || (pz > (z+1)) ) { reset(p.getName()); continue; }
			}
			if(!anyItem)
			{//筛选 物品
				if(p.getInventory().getItemInMainHand() .equals( data)) {}else{reset(p.getName());continue;}
			}
			if(PlayerTimeMap.get(p.getName())!=null && PlayerTimeMap.get(p.getName()) <= 0 )
			{//是否计时已经归零
				PlayerTimeMap.put(p.getName(), time);
				List<String> cmdList = smyhw.configer.getStringList("items."+ID+".cmd");
				for(String cmd : cmdList)
				{
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd);
				}
				continue;
			}
			if(p.isSneaking() ) 
			{//是否按了shift
				int temp1 = PlayerTimeMap.get(p.getName())-10;
				p.sendTitle(msg,"剩余时间:"+temp1);
				PlayerTimeMap.put(p.getName(), temp1);
			}
			else{reset(p.getName());continue;}
		}
	}
	
	void reset(String PlayerName)
	{//将玩家计时器重置
		PlayerTimeMap.put(PlayerName, time);
	}
}