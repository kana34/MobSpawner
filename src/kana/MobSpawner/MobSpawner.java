package kana.MobSpawner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class MobSpawner extends JavaPlugin implements Listener{
	
	private Logger logger = Logger.getLogger("Minecraft");
	public List<String> listeMobs;
	public List<String> listeregions;
	public List<Integer> listeblocid;
	private Player p;
	private String proprio;
	public Plugin plugin;
	public MSCommande commandL;
	public PluginCommand batchcommand;
	private List<String> ListeSpawner;
    
    public void onEnable(){
    	this.commandL = new MSCommande(this);
        this.batchcommand = getCommand("ms");
        batchcommand.setExecutor(commandL);
        
    	Vault.load(this);
    	Vault.setupChat();
    	Vault.setupPermissions();
    	
    	this.loadConfig();
		this.getServer().getPluginManager().registerEvents(this, this);
		logger.info("[MobSpawner] Plugin charger parfaitement!");
    }
    
    public void onDisable(){
            logger.info("[MobSpawner] Plugin stopper...");
    }
    
    public void loadConfig(){           
    	this.getConfig().options().copyDefaults(true);
		this.saveConfig();
    }
    
	@EventHandler
    public void onBlockBreak(BlockBreakEvent e){ 
    	this.p = e.getPlayer();   	
    	
    	// On vérifi si c'est un spawner
    	//------------------------------
    	if(e.getBlock().getType().equals(Material.MOB_SPAWNER)){
    		this.ListeSpawner = this.getConfig().getStringList("spawner");
    		
    		for(int ii=0; ii<ListeSpawner.size(); ii++){
    			String str[] = ListeSpawner.get(ii).split(",");
        		
        		if(Integer.parseInt(str[2]) == e.getBlock().getX() && Integer.parseInt(str[3]) == e.getBlock().getY() && Integer.parseInt(str[4]) == e.getBlock().getZ()){
        			proprio = str[1];
        			ii = ListeSpawner.size();
        		}
    		}   		
    		if(proprio != null){
    			e.setCancelled(true);
    			p.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Vous devez taper " + ChatColor.YELLOW + "/ms remove " + ChatColor.GREEN + "pour déplacer un spawner !");
    		}
    	}
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
	    for (Block block : new ArrayList<Block>(event.blockList())){
		    if(block.getType() == Material.MOB_SPAWNER) {
		    	event.blockList().remove(block);
		    }
	    }
    }
    
    WorldGuardPlugin getWorldGuard() {
        this.plugin = getServer().getPluginManager().getPlugin("WorldGuard");   
        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) { 
            getServer().getPluginManager().disablePlugin(this);
            return null; // Maybe you want throw an exception instead
        }     
        return (WorldGuardPlugin) plugin;
    }
}