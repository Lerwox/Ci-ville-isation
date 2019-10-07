package fr.lerwox.ci_ville_isation.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.lerwox.ci_ville_isation.CustomGui;
import fr.lerwox.ci_ville_isation.Main;
import fr.lerwox.ci_ville_isation.util.PlayerInfo;
import fr.lerwox.ci_ville_isation.util.Ville;

public class Commands implements CommandExecutor {
	
	public Main main;
	public CustomGui customGui;
	public Ville ville;
	public PlayerInfo player_info;
	
	public Commands(Main main, Ville ville, PlayerInfo player_vote) {
		this.main = main;
		this.ville = ville;
		this.player_info = player_vote;
		customGui = new CustomGui(main, ville, player_vote, null);
	}
	
	private int tp_delay = 0;
	private int task_tp;
	public Map<String, String> confirm_cmd = new HashMap<>();

	//TODO faire correctement les saves (genre toutes les 10 minutes, évite trop d'action sur le fichier de base de données)
	@SuppressWarnings({ "deprecation" })
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		
		ArrayList<Ville> list_cities = main.list_cities;//getCitiesList();
		ArrayList<PlayerInfo> list_player = main.list_player;//getPlayer();
		Map<String, Long> list_modif_tp = main.list_modif_tp;//getModifTp();
		
		for (PermissionAttachmentInfo perm : sender.getEffectivePermissions()) {
			if (perm.getPermission().contains("ci.admin")) {
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("ci")) {//Affiche le menu avec toutes les villes
			if((args.length == 0)) {
				if ((sender instanceof Player) && (sender.hasPermission("ci.menu"))){
					
					Player player = (Player)sender;
					int nb_lignes = (list_cities.size()/9)+1;
					
					PlayerInfo player_info = getPlayerInfo(player.getUniqueId().toString(), list_player);
					
					if(player_info == null) {
						list_player.add(new PlayerInfo(player.getUniqueId().toString(), "none", 0, 0));
						doWritePlayerInfo(list_player);
						player_info = getPlayerInfo(player.getUniqueId().toString(), list_player);
					}
					
						player.openInventory(customGui.getAccueil(nb_lignes, player.getName(), list_cities, player_info));
						
					return true;
				}
				else {
					msgToSender(sender, main.getConfig().getString("text.msg.console").replaceAll("&", "§"));
					return true;
				}
			}
			
			else {
				if(args[0].equalsIgnoreCase("vote")) {//Augmente le nombre de vote de la ville souhaité
					if(args.length == 2) {
						if (sender.hasPermission("ci.vote")) {
						
							Ville city = getCity("name", args[1], list_cities);
							
							if(city != null) {
								
								if (sender instanceof Player) {	//Si c'est un joueur
									Player player = (Player)sender;
									PlayerInfo player_info = getPlayerInfo(player.getUniqueId().toString(), list_player);
									
									if(player_info != null) {
										long timer = player_info.last_vote_time + (main.getConfig().getLong("cooldown.vote")*1000) - System.currentTimeMillis();
										
										if(!sender.hasPermission("ci.vote.cooldown.bypass") && (timer > 0)) {
											
											String msg_cooldown = WhatDate(timer/1000);
											msgToSender(sender, main.getConfig().getString("text.msg.already-vote").replaceAll("&", "§").replaceAll("%time%", msg_cooldown));
											return true;
										
										}
										
										main.getDatabaseConfig().set("player."+player_info.UUID+".last_vote_city", city.name);
										main.getDatabaseConfig().set("player."+player_info.UUID+".nb_vote", player_info.nb_vote + 1);
										main.getDatabaseConfig().set("player."+player_info.UUID+".last_vote_time", System.currentTimeMillis());
									}
									else {
										list_player.add(new PlayerInfo(player.getUniqueId().toString(), city.name, 1, System.currentTimeMillis()));
										doWritePlayerInfo(list_player);
									}
								}
								city.nb_vote++;
								
								list_cities.set(list_cities.indexOf(city), city);
								main.getDatabaseConfig().set("cities."+city.name+".nb_vote", city.nb_vote);
								main.saveDatabaseConfig();
								
								msgToSender(sender, main.getConfig().getString("text.msg.vote").replaceAll("&", "§"));
							}
							
							else {
								msgToSender(sender, main.getConfig().getString("text.msg.not-exist").replaceAll("&", "§"));
							}
						}
						else {
							msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
						}
					}
					else {
						msgToSender(sender, main.getConfig().getString("text.usage.vote").replaceAll("&", "§"));
					}
					return true;
				}
				
				
				
				if(args[0].equalsIgnoreCase("create")) {//Creation ville
					if (sender.hasPermission("ci.create")) {
						if(args.length == 3) {	//Commande correcte	
							if (args[1].contains(".") || args[1].contains(":") || args[1].contains("|") || args[1].contains("[") || args[1].contains("]") || args[1].contains("^") || args[1].contains("$") || args[1].contains("*") || args[1].contains("\\") || args[1].contains("+")|| args[1].contains("-")|| args[1].contains("/")|| args[1].contains(";") || args[1].contains("?") || args[1].contains(",")) {
								msgToSender(sender, main.getConfig().getString("text.msg.bad-char").replaceAll("&", "§"));
								return true;
							}
							
							if (args[1].length() > main.getConfig().getInt("max-size")) {
								msgToSender(sender, main.getConfig().getString("text.msg.max-limit").replaceAll("&", "§"));
								return true;
							}
							Ville city = getCity("name", args[1], list_cities);
							
							if(city != null) {
								msgToSender(sender, main.getConfig().getString("text.msg.already-exist").replaceAll("&", "§"));
								return true;
							}
							
							Player player = Bukkit.getPlayer(args[2]);
							
							if (player != null) {
								String creator_name = player.getName();
								String creator_UUID = player.getUniqueId().toString();
								
								list_cities.add(new Ville(0, args[1], creator_name, creator_UUID, "no-tp"));
							}
							else {
								list_cities.add(new Ville(0, args[1], args[2], "no-UUID", "no-tp"));
								msgToSender(sender, main.getConfig().getString("text.msg.player_disconnected").replaceAll("&", "§"));
							}
							
							msgToSender(sender, main.getConfig().getString("text.msg.create").replaceAll("&", "§"));
							doWriteCities(list_cities);
						}	
						
						else {	//Si pas le bon nombre d'arguments, (ville) (créteur)
							msgToSender(sender, main.getConfig().getString("text.usage.create").replaceAll("&", "§"));
						}
					}
					
					else {//Pas la permission
						msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
					}
					return true;
				}
				
				
				if(args[0].equalsIgnoreCase("remove")) {//Supprime une ville
					if (sender.hasPermission("ci.remove")) {
						if(args.length == 2) {	//Commande correcte
							
							Ville city = getCity("name", args[1], list_cities);
							
							if(city != null) {
								
								if(confirm_cmd.get(sender.getName()) != null && confirm_cmd.get(sender.getName()).equals("confirmed")) {
									main.getDatabaseConfig().set("cities."+city.name, null);
									main.saveDatabaseConfig();
									
									confirm_cmd.remove(sender.getName());
									msgToSender(sender, main.getConfig().getString("text.msg.remove").replaceAll("&", "§"));
								}
								else {
									String cmd_msg = "ci ";
									for(String s : args)cmd_msg += s+" ";
									MakeConfirm(sender, cmd_msg);
								}
								
							}
							else {
								msgToSender(sender, main.getConfig().getString("text.msg.not-exist").replaceAll("&", "§"));
							}
							
						}
						
						else {	//Si pas le bon nombre d'arguments, (ville)
							msgToSender(sender, main.getConfig().getString("text.usage.remove").replaceAll("&", "§"));
						}
					}
					else {//Pas la permission
						msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
					}
					
					return true;
				}
				
				
				
				if(args[0].equalsIgnoreCase("reset")) {//Remet a zero le nombre de vote d'une ville
					if (sender.hasPermission("ci.reset")) {
						if(args.length == 2) {	//Commande correcte
							
							Ville city = getCity("name", args[1], list_cities);
							
							if(city != null) {
								
								if(confirm_cmd.get(sender.getName()) != null && confirm_cmd.get(sender.getName()).equals("confirmed")) {
									main.getDatabaseConfig().set("cities."+city.name+".nb_vote", 0);
									main.saveDatabaseConfig();
									
									confirm_cmd.remove(sender.getName());
									msgToSender(sender, main.getConfig().getString("text.msg.reset").replaceAll("&", "§"));
								}
								else {
									String cmd_msg = "ci ";
									for(String s : args)cmd_msg += s+" ";
									MakeConfirm(sender, cmd_msg);
								}
								
							}
							else {
								msgToSender(sender, main.getConfig().getString("text.msg.not-exist").replaceAll("&", "§"));
							}
							
						}
						
						else {	//Si pas le bon nombre d'arguments, (ville)
							msgToSender(sender, main.getConfig().getString("text.usage.reset").replaceAll("&", "§"));
						}
					}
					else {//Pas la permission
						msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
					}
					
					return true;
				}
				
				
				
				if(args[0].equalsIgnoreCase("resetall")) {//Remet a zero le nombre de vote d'une ville
					if (sender.hasPermission("ci.reset")) {
						if(args.length == 1) {	//Commande correcte
							
							if(confirm_cmd.get(sender.getName()) != null && confirm_cmd.get(sender.getName()).equals("confirmed")) {
								for (int i = 0; i < list_cities.size(); i++) {
									main.getDatabaseConfig().set("cities."+list_cities.get(i).name+".nb_vote", 0);
								}
								
								for (int i = 0; i < list_player.size(); i++) {
									main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".nb_vote", 0);
									main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".last_vote_city", "");
									main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".last_vote_time", 0);
								}
								
								main.saveDatabaseConfig();
								confirm_cmd.remove(sender.getName());
								msgToSender(sender, main.getConfig().getString("text.msg.resetall").replaceAll("&", "§"));
							}
							else {
								String cmd_msg = "ci ";
								for(String s : args)cmd_msg += s+" ";
								MakeConfirm(sender, cmd_msg);
							}
							
						}
						
						else if(args.length == 2) {
							
							if(args[1].equalsIgnoreCase("cities")) {
								if(confirm_cmd.get(sender.getName()) != null && confirm_cmd.get(sender.getName()).equals("confirmed")) {
									for (int i = 0; i < list_cities.size(); i++) {
										
										main.getDatabaseConfig().set("cities."+list_cities.get(i).name+".nb_vote", 0);
									}
									confirm_cmd.remove(sender.getName());
									msgToSender(sender, main.getConfig().getString("text.msg.resetall-cities").replaceAll("&", "§"));
								}
								else {
									String cmd_msg = "ci ";
									for(String s : args)cmd_msg += s+" ";
									MakeConfirm(sender, cmd_msg);
								}
								
							}
							
							else if(args[1].equalsIgnoreCase("players")) {
								if(confirm_cmd.get(sender.getName()) != null && confirm_cmd.get(sender.getName()).equals("confirmed")) {
									for (int i = 0; i < list_player.size(); i++) {
										
										main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".nb_vote", 0);
										main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".last_vote_city", "none");
										main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".last_vote_time", 0);
									}
									confirm_cmd.remove(sender.getName());
									msgToSender(sender, main.getConfig().getString("text.msg.resetall-players").replaceAll("&", "§"));
								}
								else {
									String cmd_msg = "ci ";
									for(String s : args)cmd_msg += s+" ";
									MakeConfirm(sender, cmd_msg);
								}
							}
							else {
								msgToSender(sender, main.getConfig().getString("text.usage.resetall").replaceAll("&", "§"));
							}
							main.saveDatabaseConfig();
						}
						
						else {	//Si pas le bon nombre d'arguments, (ville)
							msgToSender(sender, main.getConfig().getString("text.usage.resetall").replaceAll("&", "§"));
						}
					}
					else {//Pas la permission
						msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
					}
					
					return true;
				}
				
				
				
				if(args[0].equalsIgnoreCase("removetp")) {
					if(args.length == 2)  {	//Commande correcte{
						
						Ville city = getCity("name", args[1], list_cities);
						Player player = null;
						
						if(city == null) {
							msgToSender(sender, main.getConfig().getString("text.msg.not-exist").replaceAll("&", "§"));
							return true;
						}
						
						if (sender instanceof Player) {
							player = (Player) sender;
							
							if(city.creator_UUID.equals("no-UUID") && city.creator_name.equals(player.getName())) {
								city.creator_UUID = player.getUniqueId().toString();
								main.getDatabaseConfig().set("cities."+city.name+".creator_UUID", player.getUniqueId().toString());
								main.saveDatabaseConfig();
							}
						}
						
						if (sender.hasPermission("ci.removetp") || (main.getConfig().getBoolean("creator-modif-tp") && (player.getUniqueId().toString().equals(city.creator_UUID)))) {
							
								
							main.getDatabaseConfig().set("cities."+city.name+".tp", "no-tp");
							main.saveDatabaseConfig();
							msgToSender(sender, main.getConfig().getString("text.msg.tp-remove").replaceAll("&", "§"));
						}
						else {//Pas la permission
							msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
						}
						
					}	
					else {	//Si pas le bon nombre d'arguments, (ville)
						msgToSender(sender, main.getConfig().getString("text.usage.removetp").replaceAll("&", "§"));
					}
						
					return true;
				}
				
				
				if(args[0].equalsIgnoreCase("settp")) {//Definir le point de tp d'une ville
					if  (sender instanceof Player) {
						Player player = (Player) sender;
						Ville city = getCity("name", args[1], list_cities);
						String back_msg = "tp-create";
						
						if(city == null) {
							msgToSender(sender, main.getConfig().getString("text.msg.not-exist").replaceAll("&", "§"));
							return true;
						}
						
						if(city.creator_UUID.equals("no-UUID") && city.creator_name.equals(player.getName())) {
							city.creator_UUID = player.getUniqueId().toString();
							main.getDatabaseConfig().set("cities."+city.name+".creator_UUID", player.getUniqueId().toString());
							main.saveDatabaseConfig();
						}
						
						if (player.hasPermission("ci.settp") || (main.getConfig().getBoolean("creator-modif-tp") && (player.getUniqueId().toString().equals(city.creator_UUID)))) {
							if(args.length == 2)  {	//Commande correcte
								
								if ((list_modif_tp.get(args[1]) != null)) {
									back_msg = "tp-modif";
									if (!player.hasPermission("ci.settp.cooldown.bypass")){	//non override cooldown
										Long timer = list_modif_tp.get(args[1]) + (main.getConfig().getLong("cooldown.modif-tp")*1000) - System.currentTimeMillis();
										
										if (timer > 0) {
											String msg_cooldown = WhatDate(timer/1000);
											msgToSender(sender, main.getConfig().getString("text.msg.cant-modif-tp").replaceAll("&", "§").replaceAll("%time%", msg_cooldown));
											return true;
										} 
									}
								}
								
								main.getDatabaseConfig().set("modif_tp."+args[1], System.currentTimeMillis());
								
								Location loc = player.getLocation();
								double x = loc.getX();
								double y = loc.getY();
								double z = loc.getZ();
								Vector vector = player.getEyeLocation().getDirection();
								msg =x+" "+y+" "+z+" "+ player.getWorld().getName()+" "+ vector;
								
								main.getDatabaseConfig().set("cities."+city.name+".tp", msg);
								main.getDatabaseConfig().set("modif_tp."+city.name, System.currentTimeMillis());
								main.saveDatabaseConfig();
								msgToSender(sender, main.getConfig().getString("text.msg."+back_msg).replaceAll("&", "§"));
							}
							
							else {	//Si pas le bon nombre d'arguments, (ville)
								msgToSender(sender, main.getConfig().getString("text.usage.settp").replaceAll("&", "§"));
							}
						}
						else {//Pas la permission
							msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
						}
					}
					else {
						msgToSender(sender, main.getConfig().getString("text.msg.console").replaceAll("&", "§"));
					}
					return true;
				}
				
				
				if(args[0].equalsIgnoreCase("tp")) {//Teleporte a une ville
					if (sender instanceof Player) {
						Player player = (Player) sender;
						
						if (player.hasPermission("ci.tp")) {
							if(args.length == 2){	//Commande correcte
								
								Ville city = getCity("name", args[1], list_cities);
								
								if(city != null) {
									String[] list_coord = city.tp.split(" ");
									if (list_coord.length == 5) {
										double x = Double.parseDouble(list_coord[0]);
										double y = Double.parseDouble(list_coord[1]);
										double z = Double.parseDouble(list_coord[2]);
										World w = Bukkit.getServer().getWorld(list_coord[3]);
										
										String[] value_vector = list_coord[4].split(",");
										
										Vector vector = new Vector();
										vector.setX(Double.parseDouble(value_vector[0]));
										vector.setY(Double.parseDouble(value_vector[1]));
										vector.setZ(Double.parseDouble(value_vector[2]));
			
										if (!player.hasPermission("ci.tp.cooldown.bypass")) {
											player.sendMessage(main.getConfig().getString("text.msg.tp-cooldown").replaceAll("&", "§").replaceAll("%time%", WhatDate(main.getConfig().getLong("cooldown.tp"))));
											
											Location loc = player.getLocation();
											double x_pos = loc.getX();
											double y_pos = loc.getY();
											double z_pos = loc.getZ();
											String w_pos = player.getWorld().getName();
											
											tp_delay = main.getConfig().getInt("cooldown.tp");
											task_tp = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new BukkitRunnable() {
												
												@Override
												public void run() {
													
													Location new_loc = player.getLocation();
													double new_x_pos = new_loc.getX();
													double new_y_pos = new_loc.getY();
													double new_z_pos = new_loc.getZ();
													String new_w_pos = player.getWorld().getName();
													
													if (new_x_pos != x_pos || new_y_pos != y_pos || new_z_pos != z_pos || new_w_pos != w_pos) {
														msgToSender(sender, main.getConfig().getString("text.msg.tp-cancel").replaceAll("&", "§"));
														
														Bukkit.getScheduler().cancelTask(task_tp);
													}
													
													if (tp_delay == 0) {
														//TODO safe tp
														player.teleport(new Location(w, x, y, z).setDirection(vector));
														msgToSender(sender, main.getConfig().getString("text.msg.tp").replaceAll("&", "§"));

														Bukkit.getScheduler().cancelTask(task_tp);
													}
													
													tp_delay--;
												}
											}, 20, 20);
										}
										else {	//Bypass cooldown
											player.teleport(new Location(w, x, y, z).setDirection(vector));
											msgToSender(sender, main.getConfig().getString("text.msg.tp").replaceAll("&", "§"));
										}
									}
									else {
										msgToSender(sender, main.getConfig().getString("text.msg.no-tp").replaceAll("&", "§"));
									}
								}
								else {
									msgToSender(sender, main.getConfig().getString("text.msg.not-exist").replaceAll("&", "§"));
								}
								
							}
							
							else {	//Si pas le bon nombre d'arguments, (ville) (créteur)
								msgToSender(sender, main.getConfig().getString("text.usage.tp").replaceAll("&", "§"));
							}
						}
						else {	//Pas la permission
							msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
						}
					}
					else {
						msgToSender(sender, main.getConfig().getString("text.msg.console").replaceAll("&", "§"));
					}
					return true;
				}
				
				
				if(args[0].equalsIgnoreCase("confirm")) {
					String cmd_msg = confirm_cmd.get(sender.getName());
					if(cmd_msg != null) {
						confirm_cmd.replace(sender.getName(), "confirmed");
						if(sender instanceof Player) {
							((Player) sender).chat("/"+cmd_msg);
						} else {
							Bukkit.dispatchCommand(sender, cmd_msg);
						}
					}
					else {
						msgToSender(sender, main.getConfig().getString("text.msg.nothing-conf").replaceAll("&", "§"));
					}
					return true;
				}
				
				
				if(args[0].equalsIgnoreCase("help")) {
					//TODO help en fonction des permission qu'a le joueur
					if (sender instanceof Player) {
						Player player = (Player) sender;
					
						if (player.hasPermission("ci.help")) {
							player.sendMessage(main.getConfig().getString("text.help.title").replaceAll("&", "§"));
							player.sendMessage("");
							
							Set<String> set_help = main.getConfig().getConfigurationSection("text.help").getKeys(false);	//Recupère tout les element de test.usage
							String[] str_help = set_help.toArray(new String[set_help.size()]);	//Creation d'une liste avec le contenu
							
							for (int i = 1; i < str_help.length; i++) {
								String name_cmd = str_help[i];
								player.sendMessage(main.getConfig().getString("text.help."+name_cmd).replaceAll("&", "§"));
							}
							player.sendMessage("");
						}
						else {//Pas la permission
							msgToSender(sender, main.getConfig().getString("text.msg.no-permission").replaceAll("&", "§"));
						}
					}
					else {	//pas un joueur
						msgToSender(sender, main.getConfig().getString("text.msg.console").replaceAll("&", "§"));
					}
					return true;
				}
			}
		}
		msgToSender(sender, main.getConfig().getString("text.usage.ci").replaceAll("&", "§"));
		return true;
	}

	private void msgToSender(CommandSender sender, String str) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			player.sendMessage(str);
			
			if (main.getConfig().getBoolean("log_command")) {
				Bukkit.getConsoleSender().sendMessage(player.getName()+" receive : "+ str.replaceAll("§.", ""));	//retour vers la console; On sumprimme §(un caractère), pas stylisation du texte
			}
		}
		else {
			Bukkit.getConsoleSender().sendMessage(str);
		}
		
	}

	@SuppressWarnings("deprecation")
	private void MakeConfirm(CommandSender sender, String cmd_msg ) {
		String timer = WhatDate(main.getConfig().getLong("cooldown.confirm"));
		
		confirm_cmd.put(sender.getName(), cmd_msg);
		msgToSender(sender, main.getConfig().getString("text.msg.confirm").replaceAll("&", "§").replaceAll("%cmd%", "/"+cmd_msg).replaceAll("%time%", timer));
		
		Bukkit.getScheduler().runTaskLater(main, new BukkitRunnable() {	//Permet de mettre un temps de confirmation
			
			@Override
			public void run() {
				if(confirm_cmd.get(sender.getName()) != null) confirm_cmd.remove(sender.getName());
			}
		}, 20 * main.getConfig().getLong("cooldown.confirm"));
	}

	private String WhatDate(long timer) {	//recupère une valuer en seconde et dis le nombre de semaine, jours, heures, minutes, secondes
		String date ="";
		long w = timer / 604800;
		timer = timer - w*604800;
		
		long d = timer / 86400;
		timer = timer - d*86400;
		
		long h = timer / 3600;
		timer = timer - h*3600;
		
		long m = timer / 60;
		timer = timer - m*60;
		
		if (w != 0) {
			date += main.getConfig().getString("text.date.week").replaceAll("&", "§").replaceAll("%week%", String.valueOf(w));
		}
		if (d != 0) {
			date += main.getConfig().getString("text.date.day").replaceAll("&", "§").replaceAll("%day%", String.valueOf(d));
		}
		if (h != 0) {
			date += main.getConfig().getString("text.date.hour").replaceAll("&", "§").replaceAll("%hour%", String.valueOf(h));
		}
		if (m != 0) {
			date += main.getConfig().getString("text.date.minute").replaceAll("&", "§").replaceAll("%min%", String.valueOf(m));
		}
		date += main.getConfig().getString("text.date.second").replaceAll("&", "§").replaceAll("%sec%", String.valueOf(timer));
		return date;
	}
	
	public Ville getCity(String value, String str, ArrayList<Ville> list_ville) {
		Ville city = null;
		String test_str = null;
		
		for (int i = 0; i < list_ville.size(); i++) {
			switch (value) {
			case "name":
				test_str = list_ville.get(i).name;
				break;
				
			case "creator":
				test_str = list_ville.get(i).name;
				break;

			default:
				break;
			}
			
			if (str.equalsIgnoreCase(test_str)) {
				city = list_ville.get(i);
				break;
			}
		}
		
		return city;
	}
	
	public PlayerInfo getPlayerInfo(String UUID, ArrayList<PlayerInfo> list_player) {
		PlayerInfo player_info = null;
		
		for (int i = 0; i < list_player.size(); i++) {
			if(UUID.equals(list_player.get(i).UUID)) {
				player_info = list_player.get(i);
			}
		}
		
		return player_info;
	}
	
	private void doWriteCities(ArrayList<Ville> list_cities) {	//ecrit dans le fichier de config
		for(int i = 0; i < list_cities.size(); i++){
			
			main.getDatabaseConfig().set("cities."+list_cities.get(i).name+".nb_vote", list_cities.get(i).nb_vote);
			main.getDatabaseConfig().set("cities."+list_cities.get(i).name+".name", list_cities.get(i).name);
			main.getDatabaseConfig().set("cities."+list_cities.get(i).name+".creator_name", list_cities.get(i).creator_name);
			main.getDatabaseConfig().set("cities."+list_cities.get(i).name+".creator_UUID", list_cities.get(i).creator_UUID);
			main.getDatabaseConfig().set("cities."+list_cities.get(i).name+".tp", list_cities.get(i).tp);
		}
		
		main.saveDatabaseConfig();
	}
	
	private void doWritePlayerInfo(ArrayList<PlayerInfo> list_player) {
		for(int i = 0; i < list_player.size(); i++){
			main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".UUID", list_player.get(i).UUID);
			main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".last_vote_city", list_player.get(i).last_vote_city);
			main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".nb_vote", list_player.get(i).nb_vote);
			main.getDatabaseConfig().set("player."+list_player.get(i).UUID+".last_vote_time", list_player.get(i).last_vote_time);
		}
		
		main.saveDatabaseConfig();
	}

}
