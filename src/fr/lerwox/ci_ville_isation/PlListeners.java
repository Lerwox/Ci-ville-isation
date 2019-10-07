package fr.lerwox.ci_ville_isation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.lerwox.ci_ville_isation.commands.Commands;
import fr.lerwox.ci_ville_isation.util.NBTEditor;
import fr.lerwox.ci_ville_isation.util.PlayerInfo;
import fr.lerwox.ci_ville_isation.util.SortBynb_vote;
import fr.lerwox.ci_ville_isation.util.Ville;


public class PlListeners implements Listener {
	
	public Main main;
	public CustomGui customGui;
	public Commands commands;
	
	public PlListeners(Main main) {
		this.main = main;
		customGui = new CustomGui(main, null, null, null);
		commands = new Commands(main, null, null);
	}
	
	public Ville getCity(String name_ville, ArrayList<Ville> list_ville, Player player) {
		Ville city = null;
		boolean find = false;
		
		for (int i = 0; i < list_ville.size(); i++) {
			if (name_ville.equals( list_ville.get(i).name)) {
				city = list_ville.get(i);
				find = true;
				break;
			}
		}
		
		if(!find) {
			player.closeInventory();
			Requetes.remove(player.getUniqueId());
			player.sendMessage(main.getConfig().getString("text.msg.not-exist").replaceAll("&", "§"));
		}
		
		return city;
	}
	
	//UUID, Ville
	private Map<UUID, Ville> Requetes = new HashMap<>();

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Inventory inv = e.getInventory();
		Player player = (Player) e.getWhoClicked();
		ItemStack current = e.getCurrentItem();
		ArrayList<Ville> list_ville = main.list_cities;
		Collections.sort(list_ville, new SortBynb_vote().reversed());
		
		if(current == null || current.getType().equals(Material.AIR)) return;

		if (inv.getName().equals(main.getConfig().getString("text.menu.ci.title").replaceAll("&", "§"))) {	//verif que c'est bien l'inventaire du /ci
			e.setCancelled(true);	//Impossible de recup un élément sur l'inventaire
			String name_city = (String) NBTEditor.getItemTag(current, "NameCity" );
			String page = (String) NBTEditor.getItemTag(current, "Page");
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.close_button_name").replaceAll("&", "§")) {
				player.closeInventory();
				Requetes.remove(player.getUniqueId());
			}
			
			else if(page != null) {
				if((current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.prev_button_name").replaceAll("&", "§")) && (Integer.valueOf(page) == 1)) {
					player.chat("/ci");
				}
				
				else {
					ArrayList<Ville> list_cities = main.list_cities;
					ArrayList<Ville> next_list_cities = new ArrayList<>();
					for (int i = 0/*44*Integer.valueOf(page)*/; i < list_cities.size(); i++) {
						next_list_cities.add(list_cities.get(i));
					}
					int nb_lignes = (next_list_cities.size()/9)+1;
					PlayerInfo player_info = commands.getPlayerInfo(player.getUniqueId().toString(), main.list_player);
					
					if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.next_button_name").replaceAll("&", "§")) {
						player.openInventory(customGui.getPage(nb_lignes, next_list_cities, Integer.valueOf(page)+1, player.getName(), player_info));
					} 
					else if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.prev_button_name").replaceAll("&", "§")) {
						player.openInventory(customGui.getPage(nb_lignes, next_list_cities, Integer.valueOf(page), player.getName(), player_info));
					}
					
				}
			}

			else if(name_city != null) {
				Ville city = getCity(name_city, list_ville, player);
				
				if(city != null) {
					Requetes.put(player.getUniqueId(), city);
					player.openInventory(customGui.getCity(city));
				}
			}
			
		}
		
		
		
		if (inv.getName().equals(main.getConfig().getString("text.menu.city.title").replaceAll("&", "§"))){	//verif que c'est bien l'inventaire d'une ville
			e.setCancelled(true);	//Impossible de recup un élément sur l'inventaire
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.return_button_name").replaceAll("&", "§")) {
				player.chat("/ci");
				Requetes.remove(player.getUniqueId());
			}
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.city.teleportation").replaceAll("&", "§")) {
				player.openInventory(customGui.getConfirmation("tp"));
			}
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.city.vote").replaceAll("&", "§")) {
				player.openInventory(customGui.getConfirmation("vote"));
			}
			
		}
		
		
		
		if (inv.getName().equals(main.getConfig().getString("text.menu.tp.title").replaceAll("&", "§"))){
			e.setCancelled(true);	//Impossible de recup un élément sur l'inventaire
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.return_button_name").replaceAll("&", "§")) {
				player.openInventory(customGui.getCity(Requetes.get(player.getUniqueId())));
			}
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.confirm_no").replaceAll("&", "§")) {
				player.closeInventory();
				Requetes.remove(player.getUniqueId());
			}
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.confirm_yes").replaceAll("&", "§")) {
				Ville city = Requetes.get(player.getUniqueId());
				player.chat("/ci tp "+city.name);
				player.closeInventory();
				Requetes.remove(player.getUniqueId());
			}
		}
		
		
		
		if (inv.getName().equals(main.getConfig().getString("text.menu.vote.title").replaceAll("&", "§"))) {
			e.setCancelled(true);	//Impossible de recup un élément sur l'inventaire
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.return_button_name").replaceAll("&", "§")) {
				player.openInventory(customGui.getCity(Requetes.get(player.getUniqueId())));
			}
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.confirm_no").replaceAll("&", "§")) {
				player.closeInventory();
				Requetes.remove(player.getUniqueId());
			}
			
			if(current.getItemMeta().getDisplayName() == main.getConfig().getString("text.menu.confirm_yes").replaceAll("&", "§")) {
				Ville city = Requetes.get(player.getUniqueId());
				player.chat("/ci vote "+city.name);
				player.closeInventory();
				Requetes.remove(player.getUniqueId());
			}
		}
	}
}
