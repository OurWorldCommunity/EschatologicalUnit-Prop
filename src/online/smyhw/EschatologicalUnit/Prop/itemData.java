package online.smyhw.EschatologicalUnit.Prop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

class itemData extends BukkitRunnable {
    public Boolean disableFlag = false;

    //需要匹配的物品
    public ItemStack data;
    //总时间
    public int time;
    //记录玩家剩余时间
    public HashMap<String, Integer> PlayerTimeMap = new HashMap<String, Integer>();
    public String ID;
    public String msg;
    public boolean anyItem;
    public boolean anyLoc;
    //是否消耗物品
    public boolean consume;
    public World world;
    //是否仅判断世界
    public Boolean onlyWorld;
    public int x;
    public int y;
    public int z;
    //该项目是否激活
    public boolean activation = true;

    //玩家需要的权限，为null则不检查
    public String permission;
    //若权限不足，显示的提示信息
    public String permission_denied_msg;

    public int cycle_time;

    List<String> cmdList;

    public itemData(String ID,int delay_index) {
        this.ID = ID;
        data = smyhw.configer.getItemStack("items." + ID + ".data");
        time = smyhw.configer.getInt("items." + ID + ".time",6);
        anyItem = smyhw.configer.getBoolean("items." + ID + ".anyItem",false);
        anyLoc = smyhw.configer.getBoolean("items." + ID + ".anyLoc",true);
        consume = smyhw.configer.getBoolean("items." + ID + ".consume",false);
        String worldName = smyhw.configer.getString("items." + ID + ".Loc.world","world");
        world = Bukkit.getWorld(worldName);
        if(world==null){
            smyhw.loger.warning("注意，<"+ID+">所指定的坐标范围中的世界不存在！("+worldName+")");
            return;
        }
        onlyWorld =smyhw.configer.getBoolean("items." + ID + ".Loc.only_world",true);
        x = smyhw.configer.getInt("items." + ID + ".Loc.x");
        y = smyhw.configer.getInt("items." + ID + ".Loc.y");
        z = smyhw.configer.getInt("items." + ID + ".Loc.z");
        cycle_time = smyhw.configer.getInt("items." + ID + ".cycle_time",10);
        msg = smyhw.configer.getString("items." + ID + ".msg","使用中喵~");
        cmdList = smyhw.configer.getStringList("items." + ID + ".cmd");
        activation = smyhw.configer.getBoolean("items." + ID + ".activation",true);
        permission_denied_msg = smyhw.configer.getString("items." + ID + ".permission_denied_msg","PERMISSION DENIED/权限不足");
        smyhw.loger.info("加载物品<"+ID+">,检测间隔 -> "+cycle_time+"ticks * "+time+" = "+(cycle_time*time/20.0)+"秒");
        if(data == null){
            smyhw.loger.warning("注意，<"+ID+">所指定的物品信息无效！");
            return;
        }
        this.runTaskTimer(smyhw.smyhw_, delay_index, cycle_time);
    }

    @Override
    public void run() {
        if(disableFlag){
            return;
        }
        if (!activation) {
            return;
        }
        Collection<? extends Player> player_list = Bukkit.getOnlinePlayers();
        for (Player p : player_list) {
            //权限检查
            if((this.permission != null) && (!p.hasPermission(this.permission))){
                p.sendTitle(permission_denied_msg,"");
                continue;
            }
            //新上线的玩家，初始化其的剩余时间
            if(PlayerTimeMap.get(p.getName())==null){
                PlayerTimeMap.put(p.getName(), time);
                continue;
            }
            //坐标检查
            if (!anyLoc) {
                Location temp2 = p.getLocation();
                if (!onlyWorld){
                    int px = (int) temp2.getX();
                    int py = (int) temp2.getY();
                    int pz = (int) temp2.getZ();
                    if ((px < (x - 1)) || (px > (x + 1))) {
                        reset(p.getName());
                        continue;
                    }
                    if ((py < (y - 1)) || (py > (y + 1))) {
                        reset(p.getName());
                        continue;
                    }
                    if ((pz < (z - 1)) || (pz > (z + 1))) {
                        reset(p.getName());
                        continue;
                    }
                }
                if (world != temp2.getWorld()){
                    reset(p.getName());
                    continue;
                }
            }
            //手持物品检查
            if (!anyItem) {
                ItemStack Player_inv = p.getInventory().getItemInMainHand();
                ItemStack tar = data.clone();
                if (!Player_inv.equals(tar)) {
                    //将目标物品堆数量与玩家物品堆同步
                    tar.setAmount(Player_inv.getAmount());
                    //如果还是不等，那就gg
                    if (!Player_inv.equals(tar)) {
                        reset(p.getName());
                        continue;
                    }
                    //如果这时两个物品堆相等，那就判断玩家物品堆数量是否大于原目标物品堆
                    if (Player_inv.getAmount() < data.getAmount()) {
                        reset(p.getName());
                        continue;
                    }//如果小于，gg
                    //否则为物品匹配，继续处理
                }
            }
            //shift状态检查
            if (!p.isSneaking()) {
                reset(p.getName());
                continue;
            }

            //减去剩余时间
            int temp1 = PlayerTimeMap.get(p.getName()) - 1;
            p.sendTitle(msg, "剩余时间:" + temp1);
            PlayerTimeMap.put(p.getName(), temp1);

            //剩余时间检查
            if (PlayerTimeMap.get(p.getName()) > 0) {
                continue;
            }

            //将计时重置
            PlayerTimeMap.put(p.getName(), time);
            //扣除玩家的物品
            if (consume) {//处理消耗玩家手上的物品
                ItemStack Player_inv = p.getInventory().getItemInMainHand();
                ItemStack tar = data.clone();
                if (Player_inv.getAmount() > tar.getAmount()) {//如果玩家手上的物品比目标多，就减一下数量
                    Player_inv.setAmount(Player_inv.getAmount() - tar.getAmount());
                    p.getInventory().setItemInMainHand(Player_inv);
                } else {//否则就直接给删成AIR,反正数量不足的不会运行到这里
                    ItemStack temp3 = new ItemStack(org.bukkit.Material.AIR);
                    p.getInventory().setItemInMainHand(temp3);
                }
            }

            //执行目标指令
            for (String cmd : cmdList) {
                cmd = cmd.replaceAll("%player%", p.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }

        }
    }

    void reset(String PlayerName) {//将玩家计时器重置
        PlayerTimeMap.put(PlayerName, time);
    }
}