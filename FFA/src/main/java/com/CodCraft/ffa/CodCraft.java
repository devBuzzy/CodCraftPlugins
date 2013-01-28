package com.CodCraft.ffa;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.CodCraft.api.modules.Cac;
import com.CodCraft.api.modules.GameManager;
import com.CodCraft.api.modules.Teleport;
import com.CodCraft.api.modules.Weapons;
import com.CodCraft.api.services.CCGamePlugin;
import com.CodCraft.api.CCAPI;
import com.CodCraft.ffa.GM.Round;
import com.CodCraft.ffa.GM.GameListener;
import com.CodCraft.ffa.commands.AdminCommands;
import com.CodCraft.ffa.commands.BuddyCommand;
import com.CodCraft.ffa.commands.CacComamnd;
import com.CodCraft.ffa.commands.EndRoundCommand;
import com.CodCraft.ffa.commands.GuiCommand;
import com.CodCraft.ffa.commands.SpecCommand;
import com.CodCraft.ffa.commands.SwitchCommand;
import com.CodCraft.ffa.commands.TeamsCommand;
import com.CodCraft.ffa.commands.VoteCommand;
import com.CodCraft.ffa.listener.BlockListener;
import com.CodCraft.ffa.listener.PlayerListener;

public class CodCraft extends CCGamePlugin {
   public static Plugin plugin;
   public static HashMap<Player, Integer> PlayerKills = new HashMap<Player, Integer>();
   public static HashMap<Player, Integer> PlayerDeaths = new HashMap<Player, Integer>();
   public static HashMap<Player, Integer> PlayerWins = new HashMap<Player, Integer>();
   public static HashMap<Player, Integer> PlayerLosses = new HashMap<Player, Integer>();
   public static HashMap<String, ArrayList<Location>> Teamone = new HashMap<String, ArrayList<Location>>();
   public static HashMap<String, ArrayList<Location>> Teamtwo = new HashMap<String, ArrayList<Location>>();
   public CCAPI api;
   public CCAPI getApi() {
	return api;
}

public void setApi(CCAPI api) {
	this.api = api;
}

private Round game;

   public void onEnable() {
	   final Plugin apiplugin = this.getServer().getPluginManager().getPlugin("CodCraftAPI");
	   if(apiplugin != null) {
		   api =  (CCAPI) apiplugin;
	   } else {
		   System.out.println("Did Not Get Plugin");
	   }
      getCommand("CaC").setExecutor(new CacComamnd(this));
      getCommand("EndRound").setExecutor(new EndRoundCommand(this));
      getCommand("Spec").setExecutor(new SpecCommand(this));
      getCommand("vote").setExecutor(new VoteCommand(this));
      getCommand("a").setExecutor(new AdminCommands(this));
      getCommand("team").setExecutor(new TeamsCommand(this));
      getCommand("Gui").setExecutor(new GuiCommand(this));
      getCommand("buddy").setExecutor(new BuddyCommand(this));
      getCommand("switch").setExecutor(new SwitchCommand(this));
      new MapLoader();
      getLogger().info(MapLoader.Maps.toString());
      api.getModuleForClass(GameManager.class).SetCurrentWorld("Nuketown");
      plugin = this;
      PluginManager pm = this.getServer().getPluginManager();
      pm.registerEvents(new PlayerListener(this), this);
      pm.registerEvents(new BlockListener(this), this);
      pm.registerEvents(new GameListener(this), this);
      game = new Round(this);
      game.Lobby();
      game.MainTimer();
      spawnLoad();
      api.getModuleForClass(Teleport.class).setLocations1(Teamone);
      api.getModuleForClass(Cac.class).usedefaultspawns();
      api.getModuleForClass(Weapons.class).LoadGuns();
   }

   public void onDisable() {
      //BlockManger.AllRespawnGlassblocks();
      game.Savedata();
   }

   @SuppressWarnings("unchecked")
   private void spawnLoad() {
      File spawns = new File("./plugins/CodCraftTDM/spawns.yml");
      YamlConfiguration config = YamlConfiguration.loadConfiguration(spawns);
      for(String s : config.getConfigurationSection("Teams").getKeys(false)) {
         if(s.equalsIgnoreCase("Team1")) {
            for(String Worlds : config.getConfigurationSection("Teams.Team1").getKeys(false)) {
               World world = Bukkit.getWorld(Worlds);
               ArrayList<Location> s3 = new ArrayList<Location>();
               for(String spawn : config.getConfigurationSection("Teams.Team1." + world.getName()).getKeys(false)) {
                  Location loc = new Location(world, Double.parseDouble(config.getString("Teams.Team1." + world.getName() + "." + spawn + ".x")),
                        Double.parseDouble(config.getString("Teams.Team1." + world.getName() + "." + spawn + ".y")), Double.parseDouble(config
                              .getString("Teams.Team1." + world.getName() + "." + spawn + ".z")));
                  s3.add(loc);
                  getLogger().info(
                        "  " + spawn + ": X=" + config.getString(world.getName() + "." + spawn + ".x") + " Y="
                              + config.getString(world.getName() + "." + spawn + ".y") + " Z="
                              + config.getString(world.getName() + "." + spawn + ".z"));
                  Teamone.put(world.getName(), (ArrayList<Location>) s3.clone());
               }
            }
         } else if(s.equalsIgnoreCase("Team2")) {
            for(String Worlds : config.getConfigurationSection("Teams.Team2").getKeys(false)) {
               World world = Bukkit.getWorld(Worlds);
               ArrayList<Location> s3 = new ArrayList<Location>();
               for(String spawn : config.getConfigurationSection("Teams.Team2." + world.getName()).getKeys(false)) {

                  Location loc = new Location(world, Double.parseDouble(config.getString("Teams.Team2." + world.getName() + "." + spawn + ".x")),
                        Double.parseDouble(config.getString("Teams.Team2." + world.getName() + "." + spawn + ".y")), Double.parseDouble(config
                              .getString("Teams.Team2." + world.getName() + "." + spawn + ".z")));
                  s3.add(loc);
                  getLogger().info(
                        "  " + spawn + ": X=" + config.getString(world.getName() + "." + spawn + ".x") + " Y="
                              + config.getString(world.getName() + "." + spawn + ".y") + " Z="
                              + config.getString(world.getName() + "." + spawn + ".z"));
                  Teamtwo.put(world.getName(), (ArrayList<Location>) s3.clone());
               }
            }
         }
      }
   }

   public Round getGame() {
      return game;
   }

   public void setGame(Round game) {
      this.game = game;
   }

}
