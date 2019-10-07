package fr.lerwox.ci_ville_isation.util;

public class Ville {	//Defini des villes
	
	public int nb_vote;
	public String name;
	public String creator_name;
	public String creator_UUID;
	public String tp;
	
	public Ville(int nb_vote, String name, String creator_name, String creator_UUID, String tp) {
		this.nb_vote= nb_vote;
		this.name= name;
		this.creator_name= creator_name;
		this.creator_UUID= creator_UUID;
		this.tp= tp;
	}
	
	public String toString() {	//Pour lire ailleur
		return this.nb_vote+";"+ this.name +";" + this.creator_name+";"+ this.creator_UUID +";"+this.tp;
	}
}

