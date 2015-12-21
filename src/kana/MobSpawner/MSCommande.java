package kana.MobSpawner;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class MSCommande implements CommandExecutor
{
	private Player player;
	private Block bloc;
	private Block blocRemove;
	private Location locSpawner;
	private List<String> ListeSpawner;
	private CreatureSpawner spawner;
	private String spawnerType;
	private EntityType creature;
	private World world;
	private Vector position;
	private List<String> region;
	private List<String> set;
	private List<String> listemobs;
	private WorldGuardPlugin worldGuard;
	private RegionManager regionManager;
	private int jeton;
	private int jetonadd;
	private String joueur;
	private Plugin plugins;
	private List<String> blockBlacklist;
	private String syntax;
	private String mob;
	private String proprio;
	private List<String> listeMobs;
	
	MobSpawner plugin;
	
	public MSCommande(MobSpawner plugin){
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
    	this.player = null;
    	if(sender instanceof Player){
    		player = (Player) sender;
    	}
        if(commandLabel.equalsIgnoreCase("ms")){
	        if(args.length == 0){
	        	sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.YELLOW + "/ms help");
	        	return true;
	        }             
	        if(args.length == 1){
	        	//--------------
	        	//---- HELP ----
	        	//--------------
	        	if(args[0].equalsIgnoreCase("help")){
	        		sender.sendMessage(ChatColor.YELLOW + "--------------- HELP MobSpawner ---------------");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms help" + ChatColor.GREEN + " Obtenir l'aide");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms create [mob]" + ChatColor.GREEN + " Crée un spawner");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms remove" + ChatColor.GREEN + " Supprime un spawner");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms liste"  + ChatColor.GREEN + " Affiche la liste des mobs");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms jeton [mob]"  + ChatColor.GREEN + " Donne le nombre de jeton disponible");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms addjeton [player] [mob] [jeton]"  + ChatColor.GREEN + " Ajoute des jetons");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms deljeton [player] [mob] [jeton]" + ChatColor.GREEN + " Retire des jetons");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms addmobs [mob]" + ChatColor.GREEN + " Ajoute un mob à la WhiteList");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms delmobs [mob]" + ChatColor.GREEN + " Retire un mob à la WhiteList");
	        		sender.sendMessage(ChatColor.YELLOW + "/ms reload" + ChatColor.GREEN + " Recharge la configuration");
	        		return true;
	        	}
	        	//---------------
	        	//---- LISTE ----
	        	//---------------
	        	else if(args[0].equalsIgnoreCase("liste")){
	        		// On liste tous les mobs de la whitelist
	        		//---------------------------------------
	        		this.listemobs = this.plugin.getConfig().getStringList("mobs");
	        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + "Liste des mobs disponible");
	        		for (int i=0; i<listemobs.size(); i++){
	        			sender.sendMessage(ChatColor.GREEN + "- " + listemobs.get(i));
	        		}
	        		return true;
	        	}
	        	//----------------
	        	//---- REMOVE ----
	        	//----------------
	        	else if(args[0].equalsIgnoreCase("remove")){
	        		// On vérifi les permissions
	        		//--------------------------
	        		if(!Vault.permission.playerHas(player,"mobspawner.remove") || !Vault.permission.playerHas(player,"mobspawner.breakother") || !Vault.permission.playerHas(player,"mobspawner.admin")){
	        			player.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Vous n'avez pas la permission !");
            			return false;
	        		}
	        		
	        		// On recupere le bloc que le joueur regarde
	        		//------------------------------------------
	        		this.blocRemove = getTargetBlock(player);	        		
	        		
	        		// On verifi si c'est bien un spawner
	        		//-----------------------------------
	        		if(!blocRemove.getType().equals(Material.MOB_SPAWNER)){
	        			player.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Ce bloc n'est pas un spawner !");
            			return false;
	        		}

	        		// On verifi si c'est bien le propriètaire du spawner
	        		//---------------------------------------------------
	        		this.syntax = blocRemove.getWorld().getName() + "," + player.getName() + "," + blocRemove.getX() + "," + blocRemove.getY() + "," + blocRemove.getZ();
	        		this.ListeSpawner = this.plugin.getConfig().getStringList("spawner"); 
	        		
	        		if(!ListeSpawner.contains(syntax) && (!Vault.permission.playerHas(player,"mobspawner.breakother") || !Vault.permission.playerHas(player,"mobspawner.admin"))){
	        			player.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Ce spawner n'et pas le vôtre !");
            			return false;
	        		}	        		
	        		for(int ii=0; ii<ListeSpawner.size(); ii++){
	        			String str[] = ListeSpawner.get(ii).split(",");
		        		
		        		if(Integer.parseInt(str[2]) == blocRemove.getX() && Integer.parseInt(str[3]) == blocRemove.getY() && Integer.parseInt(str[4]) == blocRemove.getZ()){
		        			proprio = str[1];
		        			ii = ListeSpawner.size();
		        		}
	        		}
	        		if(proprio == null){
		        		player.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Ce spawner n'a pas de propriètaire !");
	        			return false;
	        		}
	        		
	        		// On récupère le type de spawner
	        		//-------------------------------
	        		this.spawner = (CreatureSpawner) blocRemove.getState();
	        		String mobRemove =  spawner.getCreatureTypeName();
	        		
	        		// On remet un jeton au propriètaire du spawner
	        		//---------------------------------------------
	        		if(this.plugin.getConfig().getBoolean("increment_jeton") == true){
	        			int jeton = this.plugin.getConfig().getInt("joueurs." + proprio + "." + mobRemove.toUpperCase());
		    			this.plugin.getConfig().set("joueurs." + proprio + "." + mobRemove.toUpperCase(), Integer.valueOf(jeton + 1));
		    			this.plugin.saveConfig();	    			
	        		}
	        		
	        		// On supprime la localisation du spawner
	        		//---------------------------------------
	        		if(ListeSpawner.size() == 1){
	        			this.plugin.getConfig().set("spawner", null);
	        			this.plugin.saveConfig();
	        		}
	        		else{
	        			ListeSpawner.remove(syntax);
	        			this.plugin.getConfig().set("spawner", ListeSpawner);
		    			this.plugin.saveConfig();
	        		}	        			        		
	        		
	    			// On supprime le spawner
	    			//-----------------------
	    			blocRemove.setType(Material.AIR);
					blocRemove.getRelative(0, 1, 0).setType(Material.AIR);
	        		
					player.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Vous avez détruit votre spawner !");
    				return true;	        			        		
	        	}
	        	//----------------
	        	//---- RELOAD ----
	        	//----------------
	        	else if(args[0].equalsIgnoreCase("reload")){
	        		if(Vault.permission.playerHas(player,"mobspawner.admin") || Vault.permission.playerHas(player,"mobspawner.reload")){
		        		this.plugin.reloadConfig();
		        		player.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Configuration rechargée !");
		        		return true;
	        		}
	        		else{
	        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Vous n'avez pas la permission pour cette commande !");
	        			return false;
	        		}
	        	}
	        	//---------------
	        	//---- PURGE ----
	        	//---------------
	        	else if(args[0].equalsIgnoreCase("purge")){
	        		if(Vault.permission.playerHas(player,"mobspawner.purge") || Vault.permission.playerHas(player,"mobspawner.admin")){
	        			return true;
	        		}	
	        	}
	        	else{
	        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Commande inconnu !");
	        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Tapez " + ChatColor.YELLOW + "/ms help");
	        		return false;
	        	}
	        }
	        else if(args.length == 2){
	        	//----------------
	        	//---- CREATE ----
	        	//----------------
	        	if(args[0].equalsIgnoreCase("create")){
	        		
	        		this.spawnerType = args[1].toUpperCase();
		        	this.bloc = getTargetBlock(player);
		        	bloc.getWorld().getName();
	        		this.locSpawner = bloc.getLocation();
	        		
	        		// On vérifi si le bloc n'est pas de l'air
	        		//----------------------------------------
	        		if(this.bloc.getType().equals(Material.AIR)){
	        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Rapprochez vous du bloc, vous êtes trop loin !");
	        			return false;
	        		}
	        		
		        	// Vérification des permissions
	        		//-----------------------------
		        	if(!Vault.permission.playerHas(player,"mobspawner.create") || !Vault.permission.playerHas(player,"mobspawner.admin")){
		        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Vous n'avez pas la permission pour créer des spawners !");
		        		return false;
		        	}
		        	
	        		// Vérification mobs pour spawner disponible dans whitelist
		        	//-----------------------------------------------------------------
		        	this.listeMobs = this.plugin.getConfig().getStringList("mobs");
		        	if(!listeMobs.contains(spawnerType)){
		        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Ce type de spawner n'est pas dans la whitelist !");
		        		return false;
		        	}
		        	
	        		// Vérification des jetons
			        //------------------------			        	
	        		this.jeton = this.plugin.getConfig().getInt("joueurs." + player.getName() + "." + spawnerType);
		        	if(jeton <= 1 && !Vault.permission.playerHas(player,"mobspawner.admin")){
		        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Vous n'avez pas assez de jeton pour '" + spawnerType + " !");
		        		return false;
		        	}
		        	
		        	// Vérification des regions
		        	//-------------------------
		        	if(VerifRegionSpawnerCreate(sender, bloc) == false){
		        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Region interdit pour la creation de spawner");
		        		return false;
		        	}
	        		
	        		// Vérification des blocs interdit
		        	//--------------------------------
		        	this.blockBlacklist = this.plugin.getConfig().getStringList("bloc_blacklist");
		        	if(blockBlacklist.contains(bloc.getType().toString())){
		        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Bloc interdit pour la creation de spawner");
		        		return false;
		        	}
		        	
		        	// Vérification du bloc au dessus
		        	//-------------------------------
		        	if(bloc.getRelative(0, 1, 0).getType() == Material.MOB_SPAWNER){
    					sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Le bloc du dessus est un spawner, impossible de créer un spawner !");
    					return false;
    				}
		        	
		        	// On ajoute la localisation du spawner au joueur
		        	//-----------------------------------------------
	        		List<String> locSpawnerPlayer = this.plugin.getConfig().getStringList("spawner");
	        		locSpawnerPlayer.add(locSpawner.getWorld().getName() + "," + player.getName() + "," + locSpawner.getBlockX() + "," + locSpawner.getBlockY() + "," + locSpawner.getBlockZ());	        		
        			this.plugin.getConfig().set("spawner", locSpawnerPlayer);
	        		this.plugin.saveConfig();
	        		
	        		// On génère le spawner
	        		//---------------------
	        		bloc.setType(Material.MOB_SPAWNER);
	        		this.spawner = (CreatureSpawner) bloc.getState();
	        		spawner.setCreatureTypeByName(spawnerType);	        			
	        		bloc.getRelative(0, 1, 0).setType(Material.BEDROCK);
	        		
	        		// On retire le jeton
	        		//-------------------
	        		if(jeton - 1 == 0){
	        			this.plugin.getConfig().set("joueurs." + player.getName() + "." + spawnerType, null);
	        			this.plugin.saveConfig();
	        			return true;
	        		}
	        		else if(Vault.permission.playerHas(player,"mobspawner.admin") && jeton == 0){
	        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "MobSpawner " + creature + " créer avec succés!!!");
		        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.YELLOW + "Aucun jeton n'a étais déduit !");			        		
		        		return true;
	        		}
	        		else{
	        			this.plugin.getConfig().set("joueurs." + player.getName() + "." + spawnerType, Integer.valueOf(jeton - 1));
        				this.plugin.saveConfig();
        				sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "MobSpawner " + creature + " créer avec succés!!!");
		        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Il vous reste " + (jeton - 1) + " jeton(s) pour " + spawnerType);			        		
		        		return true;
	        		}
	        	}
	        	//---------------
	        	//---- JETON ----
	        	//---------------
	        	else if(args[0].equalsIgnoreCase("jeton")){
	        		// On sélectionne le nombre de jeton selon le mobs renseigné
	        		//----------------------------------------------------------
					this.jeton = this.plugin.getConfig().getInt("joueurs." + player.getName() + "." + args[1]);
					sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Vous disposez de :");
					sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "" + args[1] + ": " + jeton);
					return true;
		        }
	        	//-----------------
	        	//---- ADDMOBS ----
	        	//-----------------
	        	else if(args[0].equalsIgnoreCase("addmobs")){
	        		if(Vault.permission.playerHas(player,"mobspawner.mobs") || Vault.permission.playerHas(player,"mobspawner.admin")){
		        		if(this.plugin.getConfig().getStringList("mobs").contains(args[1].toUpperCase())){
		        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Ce mob est déjà dans la liste !");
		        			return false;
		        		}
		        		else{
		        			String creatureName = args[1].toLowerCase();
		        			this.creature = EntityType.fromName(creatureName);
			        		if(creature == null){
			    				sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Mob inconnu !");
			    				return false;
			    			}
			        		else{
			        			this.listemobs = this.plugin.getConfig().getStringList("mobs");
			        			listemobs.add(args[1].toUpperCase());
			        			this.plugin.getConfig().set("mobs", listemobs);
			        			this.plugin.saveConfig();
			        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Mob ajouté à la liste !");
			        			return false;
			        		}
		        		}
	        		}
	        		else{
	        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.DARK_RED + "Vous n'avez pas la permission !");
	        			return false;
	        		}
	        	}
	        	//-----------------
	        	//---- DELMOBS ----
	        	//-----------------
	        	else if(args[0].equalsIgnoreCase("delmobs")){
	        		if(Vault.permission.playerHas(player,"mobspawner.mobs") || Vault.permission.playerHas(player,"mobspawner.admin")){
		        		if(this.plugin.getConfig().getStringList("mobs").contains(args[1].toUpperCase())){
		        			this.listemobs = this.plugin.getConfig().getStringList("mobs");
		        			listemobs.remove(args[1].toUpperCase());
		        			this.plugin.getConfig().set("mobs", listemobs);
		        			this.plugin.saveConfig();
		        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Le mob a étais retiré !");
		        			return false;
		        		}
		        		else{
			    			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Le mob n'est pas dans la liste !");
			    			return false;
		        		}
	        		}
	        		else{
	        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.DARK_RED + "Vous n'avez pas la permission !");
	        			return false;
	        		}
	        	}
	        	else{
	        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Commande inconnu !");
	        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Tapez " + ChatColor.YELLOW + "/ms help");
	        		return false;
	        	}
	        }
	        else if(args.length == 4){
	        	this.joueur = args[1];
	        	this.mob = args[2].toUpperCase();
	        	this.jetonadd = Integer.parseInt(args[3]);
	        	this.jeton = this.plugin.getConfig().getInt("joueurs." + joueur + "." + mob);
	        	//------------------
	        	//---- ADDJETON ----
	        	//------------------
	        	if(args[0].equalsIgnoreCase("addjeton")){
	        		if(player == null || Vault.permission.playerHas(player,"mobspawner.admin") || Vault.permission.playerHas(player,"mobspawner.jeton")){
	        			this.listemobs = this.plugin.getConfig().getStringList("mobs");
		        			if(listemobs.contains(mob)){
		        				this.plugin.getConfig().set("joueurs." + joueur + "." + mob, Integer.valueOf(jeton + jetonadd));
		            			this.plugin.saveConfig();
		    	        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Jeton ajouter !");	        		
		    	        		return true;
		        			}
		        			else{
		        				sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Mob interdit !");
		        				sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Tapez " + ChatColor.YELLOW + "/ms liste" + ChatColor.GREEN + " pour voir la liste des mobs autorisés");
		        				return false;
		        			}
	        		}
	        		else{
	        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Vous n'avez pas la permission !");
	        			return true;
	        		}
	        	}
	        	//------------------
	        	//---- DELJETON ----
	        	//------------------
	        	else if(args[0].equalsIgnoreCase("deljeton")){
	        		if(player == null || Vault.permission.playerHas(player,"mobspawner.admin") || Vault.permission.playerHas(player,"mobspawner.jeton")){
		        		if(jeton >= 1){
		        			this.plugin.getConfig().set("joueurs." + joueur + "." + mob.toLowerCase(), Integer.valueOf(jeton - jetonadd));
		        			this.plugin.saveConfig();
		        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Jeton retirer !");
		        			return true;
		        		}
		        		else{
		        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Aucun jeton pour ce mob !");
		        			return false;
		        		}
	        		}
	        		else{
	        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Vous n'avez pas la permission !");
	        			return true;
	        		}
	        	}
	        	else{
	        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Commande inconnu !");
	        		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Tapez " + ChatColor.YELLOW + "/ms help");
	        		return false;
	        	}
	        }
	        else{
	        	sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Commande inconnu !");
	    		sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.GREEN + "Tapez " + ChatColor.YELLOW + "/ms help");
	    		return false;
	        }
        }
		return false;
    }
	
	public Block getTargetBlock(Player player) {
		int range = this.plugin.getConfig().getInt("max_range");
	    Location loc = player.getEyeLocation();
	    org.bukkit.util.Vector dir = loc.getDirection().normalize();	 
	    Block b = null;
	 
	    for (int i = 0; i <= range; i++) {
	        b = loc.add(dir).getBlock();
	        if(!b.getType().equals(Material.AIR)){
	        	i = range;
	        }
	    }	 
	    return b;
	}
	
	public boolean VerifRegionSpawnerCreate(CommandSender sender, Block bloc){
		
		this.plugins = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		this.world = bloc.getWorld();
		
		if(plugins instanceof WorldGuardPlugin){
			
			boolean verifRegionsContains = false;
    		this.position = new Vector(bloc.getLocation().getBlockX(), bloc.getLocation().getBlockY(), bloc.getLocation().getBlockZ());
    		this.region = this.plugin.getConfig().getStringList("region");
    		this.worldGuard = this.plugin.getWorldGuard();
    		this.regionManager = worldGuard.getRegionManager(world);
    		this.set = regionManager.getApplicableRegionsIDs(position);
    		
        		for(int ii=0; ii<region.size(); ii++) {        			
        			if (set.contains(region.get(ii))) {
        				verifRegionsContains = true;
        			}
        			else{
        				verifRegionsContains = false;
        			}
        		}
        		
        		if(this.plugin.getConfig().getBoolean("worldguard_in") == true && verifRegionsContains == true){
        			return true;        			
        		}
        		else if(this.plugin.getConfig().getBoolean("worldguard_in") == true && verifRegionsContains == false){
        			return false;
        		}
        		else if(this.plugin.getConfig().getBoolean("worldguard_in") == false && verifRegionsContains == true){
        			return false;
        		}
        		else if(this.plugin.getConfig().getBoolean("worldguard_in") == false && verifRegionsContains == false){
        			return true;
        		}
        		else{
        			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Problème avec MobSpawner, contactez un admin !");
        			return false;
        		}
		}
		else{
			sender.sendMessage(ChatColor.GOLD + "[MobSpawner] " + ChatColor.RED + "Problème avec WorldGuard, contactez un admin !");
			return false;
		}		
	}
	
}
