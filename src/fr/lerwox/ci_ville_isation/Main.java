package fr.lerwox.ci_ville_isation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import fr.lerwox.ci_ville_isation.commands.Commands;
import fr.lerwox.ci_ville_isation.util.PlayerInfo;
import fr.lerwox.ci_ville_isation.util.SortBynb_vote;
import fr.lerwox.ci_ville_isation.util.Ville;


public class Main extends JavaPlugin {
	
	public ArrayList<Ville> list_cities;
	public ArrayList<PlayerInfo> list_player;
	public Map<String, Long> list_modif_tp;
	
	private void loadDatabase() {
		list_cities = getCitiesList();
		list_player = getPlayer();
		list_modif_tp = getModifTp();
		
		System.out.println("[Ci_ville_isation] Database loaded !");
	}
	
	private File databaseFile;
	private FileConfiguration databaseConfig;
	
	public FileConfiguration getDatabaseConfig() {
        return this.databaseConfig;
    }
	
	public void saveDatabaseConfig() {
		try {
			databaseConfig.save(databaseFile);
			list_cities = getCitiesList();
			list_player = getPlayer();
			list_modif_tp = getModifTp();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createCustomConfig() {
        databaseFile = new File(getDataFolder(), "database.yml");
        if (!databaseFile.exists()) {
            databaseFile.getParentFile().mkdirs();
            saveResource("database.yml", false);
         }

        databaseConfig= new YamlConfiguration();
        try {
            databaseConfig.load(databaseFile);
            System.out.println("[Ci_ville_isation] Database found !");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
	
	public Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	@Override
	public void onEnable() {
		
		getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        System.out.println("[Ci_ville_isation] Configuration loaded !");
		
        createCustomConfig();
        
		getCommand("ci").setExecutor(new Commands(this, null, null));
		
		getServer().getPluginManager().registerEvents(new PlListeners(this), this);
		
		loadDatabase();
		
		System.out.println("[Ci_ville_isation] Plugin loaded !");
	}

	@Override
	public void onDisable() {
		System.out.println("[Ci_ville_isation] Shutdown Ci_ville_isation");
	}
	
	
	
	public ArrayList<Ville> getCitiesList() {	//lis le fichier de config et en fait une liste de ville
		
		ArrayList<Ville> villes = new ArrayList<Ville>();
		
		Set<String> set_villes = this.getDatabaseConfig().getConfigurationSection("cities").getKeys(false);	//Recupère tout les element de test.usage
		String[] str_villes = set_villes.toArray(new String[set_villes.size()]);	//Creation d'une liste avec le contenu
		
		for (String s : str_villes) {
			villes.add(new Ville(this.getDatabaseConfig().getInt("cities."+s+".nb_vote"), this.getDatabaseConfig().getString("cities."+s+".name"), this.getDatabaseConfig().getString("cities."+s+".creator_name"), this.getDatabaseConfig().getString("cities."+s+".creator_UUID"), this.getDatabaseConfig().getString("cities."+s+".tp")));
		}
		
		Collections.sort(villes, new SortBynb_vote().reversed());
		
		return villes;
	}
	
	public ArrayList<PlayerInfo> getPlayer() {	//lis le fichier de config et en fait une liste de ville
		
		ArrayList<PlayerInfo> players = new ArrayList<PlayerInfo>();
		
		Set<String> set_players = this.getDatabaseConfig().getConfigurationSection("player").getKeys(false);	//Recupère tout les element de test.usage
		String[] str_players = set_players.toArray(new String[set_players.size()]);	//Creation d'une liste avec le contenu
		
		for (String s : str_players) {
			players.add(new PlayerInfo(this.getDatabaseConfig().getString("player."+s+".UUID"), this.getDatabaseConfig().getString("player."+s+".last_vote_city"), this.getDatabaseConfig().getInt("player."+s+".nb_vote"), this.getDatabaseConfig().getLong("player."+s+".last_vote_time")));
		}
		
		return players;
	}
	
	public Map<String, Long> getModifTp() {
		Map<String, Long> list_modif_tp = new HashMap<>();
		
		Set<String> set_tp = this.getDatabaseConfig().getConfigurationSection("modif_tp").getKeys(false);
		String[] str_tp = set_tp.toArray(new String[set_tp.size()]);
		
		for (String s : str_tp) {
			list_modif_tp.put(s, Long.valueOf(this.getDatabaseConfig().getString("modif_tp."+s)));
		}
		
		return list_modif_tp;
	}
}
