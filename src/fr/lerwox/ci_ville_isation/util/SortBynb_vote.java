package fr.lerwox.ci_ville_isation.util;

import java.util.Comparator;

public class SortBynb_vote implements Comparator<Ville> {	//permet de trier la liste en fonction du nombre de vote

	public int compare(Ville o1, Ville o2) {
		return o1.nb_vote - o2.nb_vote;
	} 
} 
