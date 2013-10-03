package com.admixhosting.battleroom.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.CodCraft.api.event.GameWinEvent;
import com.CodCraft.api.event.RequestJoinGameEvent;
import com.CodCraft.api.event.team.TeamPlayerGainedEvent;
import com.CodCraft.api.event.team.TeamPlayerLostEvent;
import com.CodCraft.api.model.Game;
import com.CodCraft.api.model.Team;
import com.CodCraft.api.model.TeamPlayer;
import com.CodCraft.api.modules.GameManager;
import com.CodCraft.api.modules.ScoreBoard;
import com.CodCraft.api.modules.Weapons;
import com.admixhosting.battleroom.BattleRoom;
import com.admixhosting.battleroom.FireworkEffectPlayer;
import com.admixhosting.battleroom.game.BattleGame;
import com.admixhosting.battleroom.game.BattlePlayer;
import com.admixhosting.battleroom.game.BattleTeam;
import com.admixhosting.battleroom.states.LobbyState;
import com.admixhosting.battleroom.weapons.PermaFrost;
import com.codcraft.codcraftplayer.CCPlayer;
import com.codcraft.codcraftplayer.CCPlayerModule;
import com.codcraft.lobby.event.PlayerToLobbyEvent;

public class GameListener implements Listener {
	
	private BattleRoom plugin;
	private FireworkEffectPlayer fplayer;

	public GameListener(BattleRoom plugin) {
		this.plugin = plugin;
		fplayer = new FireworkEffectPlayer();
	}
	
	
	@EventHandler
	public void onRequest(RequestJoinGameEvent e) {
		Game<?> g = e.getGame();
		
		if(g != null) {
			if(g.getPlugin() == plugin) {
				Player p = Bukkit.getPlayer(e.getPlayer().getName());
				if(p != null) {

					if(((BattleGame)g).getInLobby().contains(p.getName())) {
						e.setCancelled(true);
						return;
					}
					if(!g.getCurrentState().getId().equalsIgnoreCase(new LobbyState(g).getId())) {
						e.setCancelled(true);
						return;
					}
					int players = ((BattleGame)g).getInLobby().size();
					if(players >= 32) {
						e.setCancelled(true);
						return;
					}
					if(players > 28 && !(p.hasPermission("battleroom.join28"))) {
						e.setCancelled(true);
						return;
					}
					//p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 5000, 1));
					p.getInventory().clear();
					((BattleGame)g).addInPlayer(p);
					ScoreBoard sb = plugin.api.getModuleForClass(ScoreBoard.class);
					sb.addPlayerToSeeBoard(p, g);

					//TODO CLEAN THIS UP!
					Map<Team, Integer> sizes = new HashMap<Team, Integer>();
					if(((BattleGame)g).requestedTeams.size() == 0 || ((BattleGame)g).requestedTeams.size() == 1) {
						for(Entry<String, Team> en : ((BattleGame)g).requestedTeams.entrySet()) {
							if(!sizes.containsKey(en.getValue())) {
								sizes.put(en.getValue(), 0);
							}
							sizes.put(en.getValue(), sizes.get(en.getValue()) + 1);
						}
						for(Team t : g.getTeams()) {
							if(!sizes.containsKey(t)) {
								sizes.put(t, 0);
							}
						}
					} else {
						for(Entry<String, Team> en : ((BattleGame)g).requestedTeams.entrySet()) {
							if(!sizes.containsKey(en.getValue())) {
								sizes.put(en.getValue(), 0);
							}
							sizes.put(en.getValue(), sizes.get(en.getValue()) + 1);
						}

					}
					Team team = null;
					int less = 0;
					for(Entry<Team, Integer> en : sizes.entrySet()) {
						if(team == null) {
							team = en.getKey();
							less = en.getValue();
						} else {
							if(less > en.getValue()) {
								team = en.getKey();
								less = en.getValue();
							}
						}
					}
					((BattleGame)g).requestedTeams.put(p.getName(), team);
					p.teleport(((BattleTeam)team).getSpawn());
					if(team.getName().equalsIgnoreCase("Blue")) {
						p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 11));
					} else {
						p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (short) 14));
					}
				}

				
			}
		}
	}
	
	@EventHandler
	public void onChange(WeatherChangeEvent e) {
	    GameManager gm = plugin.api.getModuleForClass(GameManager.class);
	    for (Game<?> g : gm.getAllGames())
	    	if ((g.getName().equalsIgnoreCase(e.getWorld().getName())) && (e.toWeatherState()))
	    		e.setCancelled(true);
	  }
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onJoin(final TeamPlayerGainedEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		final Game<?> g = gm.getGameWithTeam(e.getTeam());
		if(g != null) {
			if(g.getPlugin() == plugin) {
				final Player p = Bukkit.getPlayer(e.getPlayer().getName());
				p.setAllowFlight(true);
				p.setFlying(true);

				Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
					
					@Override
					public void run() {
						List<String> list = new ArrayList<>();
						list.add("Lazer");
						list.add("SaveYourSkin");
						BattlePlayer bp = (BattlePlayer) e.getPlayer();
						if(!bp.medic) {
							list.add("permafrost");
							
						} else {
							list.add("DragonsBreath");
							list.add("hook");

						}

						plugin.api.getModuleForClass(Weapons.class).requestWeapons(p, list);
						
					}
				}, 1);
				if(e.getTeam().getName().equalsIgnoreCase("Blue")) {
					p.getInventory().setItem(8, new ItemStack(Material.WOOL, 1, (short) 11));
					p.updateInventory();
				} else {
					p.getInventory().setItem(8, new ItemStack(Material.WOOL, 1, (short) 14));
					p.updateInventory();
				}

			}
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		Game<?> g = gm.getGameWithPlayer(e.getPlayer());
		for(Game<?> game : gm.getGamesForPlugin(plugin)) {
			BattleGame bp = (BattleGame) game;
			if(bp.getInLobby().contains(e.getPlayer().getName())) {
				e.setCancelled(true);
			}
		}
		if(g != null) {
			if(g.getPlugin() == plugin) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		Game<?> g = gm.getGameWithPlayer(e.getPlayer());
		for(Game<?> game : gm.getGamesForPlugin(plugin)) {
			BattleGame bp = (BattleGame) game;
			if(bp.getInLobby().contains(e.getPlayer().getName())) {
				e.setCancelled(true);
			}
		}
		if(g != null) {
			if(g.getPlugin() == plugin) {
				e.setCancelled(true);
			}
		}
	}
	
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		boolean broadcast = false;
		boolean perm = false;
		boolean normal = false;
		boolean unfrozen = false;
		boolean player1 = false;
		if(e.getEntity() instanceof Player) {
			GameManager gm = plugin.api.getModuleForClass(GameManager.class);
			Game<?> g = gm.getGameWithPlayer((Player) e.getEntity());
			if(g != null) {
				if(g.getPlugin() == plugin) {
					if(e instanceof EntityDamageByEntityEvent) {
						EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
						Player p = (Player) e.getEntity();
						if(event.getDamager() instanceof Player) {
							Player d = (Player) event.getDamager();
							if(d.getItemInHand().getType() == Material.NAME_TAG) {
								BattlePlayer bp = (BattlePlayer) g.findTeamWithPlayer(d).findPlayer(d);
								if(bp.medic) {
									BattlePlayer bpp = (BattlePlayer) g.findTeamWithPlayer(p).findPlayer(p);
									if(!bpp.getFrozen()) {
										broadcast = true;
										normal = false;
										player1 = true;
										if(!g.findTeamWithPlayer(p).getName().equalsIgnoreCase(g.findTeamWithPlayer(d).getName())) {
											bpp.setFrozenpoint(p.getLocation());
											bpp.setFrozen(true);
											CCPlayerModule ccpm = plugin.api.getModuleForClass(CCPlayerModule.class);
											CCPlayer player = ccpm.getPlayer(d);
											player.setFreezes(player.getFreezes() + 1);
											bp.setFrozens(bp.getFrozens() + 1);
										}
									}



								}
							}

						} else if(((EntityDamageByEntityEvent) e).getDamager() instanceof Projectile) {
							Player d = (Player) ((Projectile) event.getDamager()).getShooter();
							if(g.findTeamWithPlayer(p).getId().equalsIgnoreCase(g.findTeamWithPlayer(d).getId())) {
								BattlePlayer bpd = (BattlePlayer) g.findTeamWithPlayer(d).findPlayer(d);
								BattlePlayer bp = (BattlePlayer) g.findTeamWithPlayer(p).findPlayer(p);
								if(bp.getFrozen()) {
									if(bpd.medic) {
										bp.setFrozen(false);
										CCPlayerModule ccpm = plugin.api.getModuleForClass(CCPlayerModule.class);
										CCPlayer player = ccpm.getPlayer(d);
										player.setUnfreezes(player.getUnfreezes() + 1);
										broadcast = true;
										unfrozen = true;
									}
								}
							} else {
								BattlePlayer bpd = (BattlePlayer) g.findTeamWithPlayer(d).findPlayer(d);
								BattlePlayer bp = (BattlePlayer) g.findTeamWithPlayer(p).findPlayer(p);
								if(bpd.medic) {
									return;
								}

								PermaFrost weapon = (PermaFrost) plugin.api.getModuleForClass(Weapons.class).getWeapon("PermaFrost");
								if(weapon.shots.containsKey(((EntityDamageByEntityEvent) e).getDamager())) {
									if(!bp.getFrozen() || !bp.isPermfrozen()){
										broadcast = true;
										perm = true;
										bp.setFrozenpoint(p.getLocation());
										bp.setPermfrozen(true);
										CCPlayerModule ccpm = plugin.api.getModuleForClass(CCPlayerModule.class);
										CCPlayer player = ccpm.getPlayer(d);
										player.setFreezes(player.getFreezes() + 1);
										bp.setFrozens(bp.getFrozens() + 1);
										d.getInventory().remove(Material.ICE);
										weapon.shots.remove(((EntityDamageByEntityEvent) e).getDamager());
										List<Entity> toRemove = new ArrayList<>();
										for(Entry<Entity, String> en : weapon.shots.entrySet()) {
											if(en.getValue().equalsIgnoreCase(d.getName())) {
												toRemove.add(en.getKey());
												en.getKey().remove();
											}
										}
										for(Entity en : toRemove) {
											weapon.shots.remove(en);
										}
									}
								}
								if(!bp.isPermfrozen() && !bp.getFrozen()) {
									broadcast = true;
									normal = true;
									((BattlePlayer)g.findTeamWithPlayer(p).findPlayer(p)).setFrozenpoint(p.getLocation());
									((BattlePlayer)g.findTeamWithPlayer(p).findPlayer(p)).setFrozen(true);//(p.getLocation().getDirection());
									CCPlayerModule ccpm = plugin.api.getModuleForClass(CCPlayerModule.class);
									CCPlayer player = ccpm.getPlayer(d);
									player.setFreezes(player.getFreezes() + 1);
									bp.setFrozens(bp.getFrozens() + 1);
								}



							}
						}
						if(broadcast) {
							String message = "";
							Player effect = null;
							if(perm) {
								Player d = (Player) ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
								message = g.findTeamWithPlayer(d).getColor() + d.getName() + ChatColor.WHITE + " perm-froze " + g.findTeamWithPlayer(p).getColor() + p.getName();
								effect = p;
								
							} else if(normal) {
								Player d = (Player) ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
								message = g.findTeamWithPlayer(d).getColor() + d.getName() + ChatColor.WHITE + " froze " + g.findTeamWithPlayer(p).getColor() + p.getName();
								effect = p;
							} else if(unfrozen) {
								Player d = (Player) ((Projectile) ((EntityDamageByEntityEvent) e).getDamager()).getShooter();
								message = g.findTeamWithPlayer(d).getColor() + d.getName() + ChatColor.WHITE + " un-froze " + g.findTeamWithPlayer(p).getColor() + p.getName();
								effect = p;
							} else if(player1) {
								Player d = (Player) ((EntityDamageByEntityEvent) e).getDamager();
								message = g.findTeamWithPlayer(d).getColor() + d.getName() + ChatColor.WHITE + " froze " + g.findTeamWithPlayer(p).getColor() + p.getName();
								effect = d;
							}
							for(Team t : g.getTeams()) {
								for(TeamPlayer tp : t.getPlayers()) {
									Player p1 = Bukkit.getPlayer(tp.getName());
									if(p1 != null) {
										p1.sendMessage(message);
										try {
											fplayer.playFirework(effect.getWorld(), effect.getLocation(), FireworkEffect.builder().withColor(((BattleTeam)g.findTeamWithPlayer(effect)).getColorNew()).build());
										} catch (Exception e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										
									}
								}
							}
						}

						Team t = g.findTeamWithPlayer(p);
						Team t1 = null;
						for(Team t2 : g.getTeams()) {
							if(!t2.containsPlayer(p)) {
								t1 = t2;
							}
						}
						int size = t.getPlayers().size();
						int frozen = 0;
						for(TeamPlayer tp : t.getPlayers()) {
							BattlePlayer bp1 = (BattlePlayer) tp;
							if(bp1.getFrozen() || bp1.isPermfrozen()) {
								frozen++;
							}
						}
						if(frozen >= size) {
							GameWinEvent event1 = new GameWinEvent(t1.getName() + " has won the game!", t1, g);
							Bukkit.getPluginManager().callEvent(event1);
							Bukkit.broadcastMessage(event1.getWinMessage());
						}
						e.setCancelled(true);
					}

				}
			}
		}

	}

	
	@EventHandler
	public void onInt(PlayerInteractEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		Game<?> g = gm.getGameWithPlayer(e.getPlayer());
		if(g != null) {
			if(g.getPlugin() == plugin) {
				if(e.getClickedBlock() != null) {
					if(e.getClickedBlock().getType() == Material.BEACON) {
						e.setCancelled(true);
					}
				}

			}
		}

	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		Game<?> g = gm.getGameWithPlayer(e.getPlayer());
		if(g != null) {
			if(g.getPlugin() == plugin) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void toLobby(PlayerToLobbyEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		for(Game<?> g : gm.getGamesForPlugin(plugin)) {
			BattleGame bg = (BattleGame) g;
			if(bg.getInLobby().contains(e.getPlayer().getName())) {
				bg.removeInLobby(e.getPlayer());
				TeamPlayerLostEvent event = new TeamPlayerLostEvent(bg.requestedTeams.get(e.getPlayer().getName()), new BattlePlayer(e.getPlayer().getName()));
				Bukkit.getPluginManager().callEvent(event);
			}
		}
	}
	
	@EventHandler
	public void onLose(TeamPlayerLostEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		Game<?> g = gm.getGameWithTeam(e.getTeam());
		if(g != null) {
			if(g.getPlugin() == plugin) {
				BattlePlayer bp = (BattlePlayer) e.getPlayer();
				plugin.api.getModuleForClass(ScoreBoard.class).removePlayerFromScoreBoard(Bukkit.getPlayer(bp.getName()));
				Bukkit.getPlayer(bp.getName()).getInventory().setHelmet(null);
				Player p = Bukkit.getPlayer(bp.getName());
				if(p != null) {
					p.performCommand("spawn");
					BattleGame bg = (BattleGame) g;
					bg.requestedTeams.remove(p.getName());
					bg.removeInLobby(p);
				}
			}
		}
	}
	
	@EventHandler
	public void onSpawn(CreatureSpawnEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		
		for(Game<?> g : gm.getGamesForPlugin(plugin)) {
			if(g.getName().equalsIgnoreCase(e.getLocation().getWorld().getName())) {
				e.setCancelled(true);
			}
		}

	}
	
	@EventHandler
	public void onWin(GameWinEvent e) {
		if(e.getGame() != null) {
			if(e.getGame().getPlugin() == plugin){
				for(final Team t : e.getGame().getTeams()) {
					if(e.getTeam() != null) {
						if(t.getId().equalsIgnoreCase(e.getTeam().getId())) {
							for(TeamPlayer tp : t.getPlayers()) {
								BattlePlayer bp = (BattlePlayer) tp;
								CCPlayerModule ccpm = plugin.api.getModuleForClass(CCPlayerModule.class);
								CCPlayer player = ccpm.getPlayer(tp.getName());
								player.setBGwins(player.getBGwins() + 1);
								player.incrementCredit(10);
								player.incrementCredit(bp.getFrozens() + bp.getUnfrozen());
							}
						} else {
							for(TeamPlayer tp : t.getPlayers()) {
								BattlePlayer bp = (BattlePlayer) tp;
								CCPlayerModule ccpm = plugin.api.getModuleForClass(CCPlayerModule.class);
								CCPlayer player = ccpm.getPlayer(tp.getName());
								player.setBGlosses(player.getBGlosses() + 1);
								player.incrementCredit(bp.getFrozens() + bp.getUnfrozen());
							}
						}
					}
					
					for(final TeamPlayer tp : t.getPlayers()) {
						final BattlePlayer bp = (BattlePlayer) tp;
						final Player p = Bukkit.getPlayer(bp.getName());
						p.getInventory().clear();
						Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
							
							@Override
							public void run() {

								t.removePlayer(tp);
								bp.setFrozen(false);
								bp.setOnWall(false);
								bp.setOldLoc(null);
								plugin.api.getModuleForClass(ScoreBoard.class).removePlayerFromScoreBoard(Bukkit.getPlayer(bp.getName()));
								Bukkit.getPlayer(bp.getName()).getInventory().setHelmet(null);
								p.performCommand("lobby");
								p.getInventory().clear();
							}
						}, 200);
					}
				}
				e.getGame().deinitialize();
				e.getGame().initialize();
				e.getGame().setState(new LobbyState(e.getGame()));
			}
		}
	}
	
	@EventHandler
	public void onFade(BlockFadeEvent e) {
		GameManager gm = plugin.api.getModuleForClass(GameManager.class);
		List<Game<?>> games = gm.getGamesForPlugin(plugin);
		for(Game<?> g : games) {
			if(g.getName().equalsIgnoreCase(e.getBlock().getWorld().getName())) {
				e.setCancelled(true);
			}
		}
	}
	

}