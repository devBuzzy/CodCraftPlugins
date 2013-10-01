package com.codcraft.lobby;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.CodCraft.api.event.RequestJoinGameEvent;
import com.CodCraft.api.event.team.TeamPlayerLostEvent;
import com.CodCraft.api.model.Game;
import com.CodCraft.api.model.Team;
import com.CodCraft.api.model.TeamPlayer;
import com.CodCraft.api.modules.GameManager;
import com.codcraft.ccommands.PlayerDoSpawnEvent;
import com.codcraft.lobby.event.PlayerEnterLobbyEvent;
import com.codcraft.lobby.event.PlayerToLobbyEvent;

public class LobbyListener implements Listener {
	private CCLobby plugin;
	public LobbyListener(CCLobby plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public void pjoin(PlayerJoinEvent e) {
		
		PlayerToLobbyEvent event = new PlayerToLobbyEvent(e.getPlayer());
		Bukkit.getPluginManager().callEvent(event);
	}
	
	@EventHandler
	public void onLeave(TeamPlayerLostEvent e) {
		PlayerToLobbyEvent event = new PlayerToLobbyEvent(e.getPlayer());
		Bukkit.getPluginManager().callEvent(event);
	}

	
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent e) {
		GameManager gamemanger = plugin.CCAPI.getModuleForClass(GameManager.class);

		for(Entry<Integer, Lobby> lobby : plugin.configmap.entrySet()) {
			if(isInside(e.getPlayer(),lobby.getValue().getBlock1().getX(),lobby.getValue().getBlock1().getY(),lobby.getValue().getBlock1().getZ(),lobby.getValue().getBlock2().getX(),lobby.getValue().getBlock2().getY(),lobby.getValue().getBlock2().getZ())){
				@SuppressWarnings("unused")
				Team teama = null;
				if(lobby.getValue().getGame() == null) {
					return;
				}
				Game<?> game = null;
				for(Game<?> g : gamemanger.getAllGames()) {
					if(g.getName().equalsIgnoreCase(lobby.getValue().getGame())) {
						game = g;
					}
				}
				if(game == null) {
					return;
				}
				for(Team team :game.getTeams()) {
					if(team.containsPlayer(e.getPlayer())) {
						teama = team;
					}
				}
				TeamPlayer p = new TeamPlayer(e.getPlayer().getName());
				PlayerEnterLobbyEvent event = new PlayerEnterLobbyEvent(p,lobby);
				Bukkit.getPluginManager().callEvent(event);
				
				RequestJoinGameEvent revent = new RequestJoinGameEvent(p, game, null);
				Bukkit.getPluginManager().callEvent(revent);
		        LobbyModule lm = (LobbyModule)this.plugin.CCAPI.getModuleForClass(LobbyModule.class);
		        
		        lm.UpdateSign(lm.getLobby(game.getName()));				
		        if(revent.getTeam() == null) {
					return;
				}
				Team t = revent.getTeam();
				t.addPlayer(revent.getPlayer());
					
			} else {
			}
		}
		
	}
	
	@EventHandler
	public void onDisbatch(PlayerDoSpawnEvent e) {
		PlayerToLobbyEvent event = new PlayerToLobbyEvent(e.getPlayer());
		Bukkit.getPluginManager().callEvent(event);
		if(e.getGame() == null) {
			return;
		}
	    LobbyModule lm = (LobbyModule)this.plugin.CCAPI.getModuleForClass(LobbyModule.class);
	    lm.UpdateSign(lm.getLobby(e.getGame().getName()));
	}
	

	@EventHandler
	public void playerquit(PlayerQuitEvent e) {
	}
	
	   private boolean isInside(Player p, double maxX, double maxY, double maxZ, double minX, double minY, double minZ) {
		   Location loc = p.getLocation();
		   if(loc.getX() < minX || loc.getX() > maxX)
			   return false;
		   if(loc.getZ() < minZ || loc.getZ() > maxZ)
			   return false;
		   if(loc.getY() < minY || loc.getY() > maxY)
			   return false;
		   return true;
	   }
	
	

}
