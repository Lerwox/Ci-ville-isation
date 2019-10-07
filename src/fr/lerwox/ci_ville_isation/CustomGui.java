package fr.lerwox.ci_ville_isation;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import fr.lerwox.ci_ville_isation.util.NBTEditor;
import fr.lerwox.ci_ville_isation.util.PlayerInfo;
import fr.lerwox.ci_ville_isation.util.Ville;

public class CustomGui{
	
	public Main main;
	public Ville ville;
	public PlayerInfo player_info;
	public NBTEditor NBT_editor;
	
	public CustomGui(Main main, Ville ville, PlayerInfo player_vote, NBTEditor NBT_editor) {
		this.main = main;
		this.ville = ville;
		this.player_info = player_vote;
		this.NBT_editor = NBT_editor;
	}
	
	//permet recup tete du joueur
	public static ItemStack getSkull(String pseudo, String city_name, String[] lore) {
		ArrayList<String> lores = new ArrayList<String>();
		for(String part:lore) {
			lores.add(part);
		}
		ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta itM = (SkullMeta) it.getItemMeta();
		itM.setOwner(pseudo);
		itM.setDisplayName(city_name);
		itM.setLore(lores);
		it.setItemMeta(itM);
		
		return it;
	}
	
	//recup item
	public static ItemStack getItem(ItemStack it, String customName, String[] lore) {
		ItemMeta itM = it.getItemMeta();
		if(lore != null) {
			ArrayList<String> lores = new ArrayList<String>();
			for(String part:lore) {
				lores.add(part);
			}
			itM.setLore(lores);
		}
		if(customName != null) itM.setDisplayName(customName);
		it.setItemMeta(itM);
		
		return it;
	}
	
	//TODO Personnalisé item via le fichier de config
	public Inventory getAccueil(int nb_lignes, String pseudo, ArrayList<Ville> list_cities, PlayerInfo player_info) {	//retourne le menu du /ci
		
		String name = main.getConfig().getString("text.menu.ci.title").replaceAll("&", "§");
		Boolean next = false;
		
		if(nb_lignes > 5) {
			nb_lignes = 5;
			next = true;
		}
		
		int nb_case = (nb_lignes+1)*9;
		
		Inventory accueil = Bukkit.createInventory(null, nb_case, name);
		
		for (int i = 0; i < list_cities.size(); i++) {	//Créer les items pour les villes
			String[] lores = new String[2];
			lores[0] = main.getConfig().getString("text.menu.ci.creator").replaceAll("&", "§").replaceAll("%creator%", list_cities.get(i).creator_name);
			lores[1] = main.getConfig().getString("text.menu.ci.nb_vote").replaceAll("&", "§").replaceAll("%nb_vote%", String.valueOf(list_cities.get(i).nb_vote));
			
			ItemStack it = getSkull(list_cities.get(i).creator_name, main.getConfig().getString("text.menu.ci.city").replaceAll("&", "§").replaceAll("%city%", list_cities.get(i).name), lores);
			it = NBTEditor.setItemTag(it, list_cities.get(i).name, "NameCity");
			
			accueil.setItem(i, it);	//Set dans l'inventaire
		}
		
		String[] lores = new String[2];
		lores[0] = main.getConfig().getString("text.menu.ci.info.nb_vote").replaceAll("&", "§").replaceAll("%nb_vote%", String.valueOf(player_info.nb_vote));
		lores[1] = main.getConfig().getString("text.menu.ci.info.last_vote").replaceAll("&", "§").replaceAll("%city%", player_info.last_vote_city);
		
		accueil.setItem(nb_case - 5, getSkull(pseudo, main.getConfig().getString("text.menu.ci.info.name").replaceAll("&", "§"), lores));
		
		String[] desc = main.getConfig().getString("text.menu.close_button_desc").replaceAll("&", "§").split("\n");
		accueil.setItem(nb_case - 1, getItem(new ItemStack(Material.BARRIER), main.getConfig().getString("text.menu.close_button_name").replaceAll("&", "§"), desc));
		
		if(next) {
			desc = main.getConfig().getString("text.menu.next_button_desc").replaceAll("&", "§").split("\n");
			ItemStack it = getItem(new ItemStack(Material.ARROW), main.getConfig().getString("text.menu.next_button_name").replaceAll("&", "§"), desc);
			it = NBTEditor.setItemTag(it, "1", "Page");
			accueil.setItem(nb_case-3, it);
		}
		
		for (int i = nb_case-9; i < nb_case; i++) {
			if(accueil.getItem(i) == null) {
				accueil.setItem(i, getItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14), " ", null));
			}
		}
		
		return accueil;
	}
	
	
	public Inventory getPage(Integer nb_lignes, ArrayList<Ville> list_cities, Integer id_page, String pseudo, PlayerInfo player_info) {
		String name = main.getConfig().getString("text.menu.ci.title").replaceAll("&", "§");
		Boolean next = false;
		
		if(nb_lignes > 5) {
			nb_lignes = 5;
			next = true;
		}
		
		int nb_case = (nb_lignes+1)*9;
		
		Inventory invPage = Bukkit.createInventory(null, nb_case, name);
		
		for (int i = 0; i < list_cities.size(); i++) {	//Créer les items pour les villes
			String[] lores = new String[2];
			lores[0] = main.getConfig().getString("text.menu.ci.creator").replaceAll("&", "§").replaceAll("%creator%", list_cities.get(i).creator_name);
			lores[1] = main.getConfig().getString("text.menu.ci.nb_vote").replaceAll("&", "§").replaceAll("%nb_vote%", String.valueOf(list_cities.get(i).nb_vote));
			
			ItemStack it = getSkull(list_cities.get(i).creator_name, main.getConfig().getString("text.menu.ci.city").replaceAll("&", "§").replaceAll("%city%", list_cities.get(i).name), lores);
			it = NBTEditor.setItemTag(it, list_cities.get(i).name, "NameCity");
			
			invPage.setItem(i, it);	//Set dans l'inventaire
		}
		
		String[] lores = new String[2];
		lores[0] = main.getConfig().getString("text.menu.ci.info.nb_vote").replaceAll("&", "§").replaceAll("%nb_vote%", String.valueOf(player_info.nb_vote));
		lores[1] = main.getConfig().getString("text.menu.ci.info.last_vote").replaceAll("&", "§").replaceAll("%city%", player_info.last_vote_city);
		
		invPage.setItem(nb_case - 5, getSkull(pseudo, main.getConfig().getString("text.menu.ci.info.name").replaceAll("&", "§"), lores));
		
		String[] desc = main.getConfig().getString("text.menu.close_button_desc").replaceAll("&", "§").split("\n");
		invPage.setItem(nb_case - 1, getItem(new ItemStack(Material.BARRIER), main.getConfig().getString("text.menu.close_button_name").replaceAll("&", "§"), desc));
		
		desc = main.getConfig().getString("text.menu.prev_button_desc").replaceAll("&", "§").split("\n");
		ItemStack it = getItem(new ItemStack(Material.ARROW), main.getConfig().getString("text.menu.prev_button_name").replaceAll("&", "§"), desc);
		it = NBTEditor.setItemTag(it, String.valueOf(id_page-1), "Page");
		invPage.setItem(nb_case-7, it);
		
		if(next) {
			desc = main.getConfig().getString("text.menu.next_button_desc").replaceAll("&", "§").split("\n");
			it = getItem(new ItemStack(Material.ARROW), main.getConfig().getString("text.menu.next_button_name").replaceAll("&", "§"), desc);
			it = NBTEditor.setItemTag(it, String.valueOf(id_page), "Page");
			invPage.setItem(nb_case-3, it);
		}
		
		for (int i = nb_case-9; i < nb_case; i++) {
			if(invPage.getItem(i) == null) {
				invPage.setItem(i, getItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14), " ", null));
			}
		}
		
		return invPage;
	}
	
	
	public Inventory getCity(Ville city) {
		
		String inv_name = main.getConfig().getString("text.menu.city.title").replaceAll("&", "§");
		
		Inventory inv_city = Bukkit.createInventory(null, InventoryType.HOPPER, inv_name);
		
		String[] desc =main.getConfig().getString("text.menu.city.city_desc").replaceAll("&", "§").replaceAll("%creator%", city.creator_name).replaceAll("%city%", city.name).split("\n");
		
		inv_city.setItem(0, getSkull(city.creator_name, main.getConfig().getString("text.menu.city.city").replaceAll("&", "§").replaceAll("%city%", city.name), desc));
		
		desc = main.getConfig().getString("text.menu.city.teleportation_desc").replaceAll("&", "§").split("\n");
		inv_city.setItem(1, getItem(new ItemStack(Material.ENDER_PEARL), main.getConfig().getString("text.menu.city.teleportation").replaceAll("&", "§"), desc));
		
		desc = main.getConfig().getString("text.menu.city.vote_desc").replaceAll("&", "§").split("\n");
		inv_city.setItem(2, getItem(new ItemStack(Material.WOOL, 1, (short) 5), main.getConfig().getString("text.menu.city.vote").replaceAll("&", "§"), desc));
		
		desc = main.getConfig().getString("text.menu.return_button_desc").replaceAll("&", "§").split("\n");
		inv_city.setItem(4, getItem(new ItemStack(Material.BARRIER), main.getConfig().getString("text.menu.return_button_name").replaceAll("&", "§"), desc));
		
		return inv_city;
	}
	

	public Inventory getConfirmation(String str) {
		
		String inv_name = main.getConfig().getString("text.menu."+str+".title").replaceAll("&", "§");
		
		Inventory inv_confirm = Bukkit.createInventory(null, InventoryType.HOPPER, inv_name);
		
		String[] desc = new String[] {main.getConfig().getString("text.menu.confirm_yes_desc").replaceAll("&", "§")};
		inv_confirm.setItem(0, getItem(new ItemStack(Material.WOOL, 1, (short) 5), main.getConfig().getString("text.menu.confirm_yes").replaceAll("&", "§"), desc));
		inv_confirm.setItem(1, getItem(new ItemStack(Material.WOOL, 1, (short) 5), main.getConfig().getString("text.menu.confirm_yes").replaceAll("&", "§"), desc));
		
		desc[0] = main.getConfig().getString("text.menu.return_button_desc").replaceAll("&", "§");
		inv_confirm.setItem(2, getItem(new ItemStack(Material.BARRIER), main.getConfig().getString("text.menu.return_button_name").replaceAll("&", "§"), desc));
		
		desc[0] = main.getConfig().getString("text.menu.confirm_no_desc").replaceAll("&", "§");
		inv_confirm.setItem(3, getItem(new ItemStack(Material.WOOL, 1, (short) 14), main.getConfig().getString("text.menu.confirm_no").replaceAll("&", "§"), desc));
		inv_confirm.setItem(4, getItem(new ItemStack(Material.WOOL, 1, (short) 14), main.getConfig().getString("text.menu.confirm_no").replaceAll("&", "§"), desc));
		
		
		return inv_confirm;
	}
}
