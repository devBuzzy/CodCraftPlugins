package com.codcraft.bunggie;


import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;

public class Listener implements net.md_5.bungee.api.plugin.Listener {

	@SuppressWarnings("unused")
	private Main plugin;

	public Listener(Main plugin) {
		this.plugin = plugin;
	}

	
	
	@EventHandler
	public void onJoin(LoginEvent e) {
		
		if(e.isCancelled()) {
			e.setCancelReason(ChatColor.RED+"Updating the Lobby. Please reconnect in a few.");
		}
	}
	@EventHandler
	public void postloginevent(final PostLoginEvent e) {
		/*plugin.timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				for(ProxiedPlayer pp: BungeeCord.getInstance().getPlayers()){
					pp.sendMessage(ChatColor.YELLOW + e.getPlayer().getName() + " has joined CylumNetwork");
				}
			}
		}, 1);*/
	}
	
	@EventHandler
	public void onLeave(final PlayerDisconnectEvent e) {
		/*plugin.timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
					p.sendMessage(ChatColor.YELLOW + "" + e.getPlayer().getName() + " has left CylumNetwork!");
				}
			}
		}, 1);*/

	}
}
