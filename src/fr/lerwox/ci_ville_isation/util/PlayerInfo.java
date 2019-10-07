package fr.lerwox.ci_ville_isation.util;

public class PlayerInfo {

	public String UUID;
	public String last_vote_city;
	public int nb_vote;
	public long last_vote_time;
	
	public PlayerInfo(String UUID, String last_vote_city, int nb_vote, long last_vote_time) {
		this.UUID= UUID;
		this.last_vote_city= last_vote_city;
		this.nb_vote= nb_vote;
		this.last_vote_time= last_vote_time;
	}
	
	public String toString() {	//Pour lire dans ailleur
		return this.UUID+";"+ this.last_vote_city +";" + this.nb_vote+";"+ this.last_vote_time;
	}
}
