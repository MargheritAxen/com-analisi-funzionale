package com.accenture.analisifunzionale.classi.liste;

import java.util.LinkedList;

import com.accenture.analisifunzionale.classi.Campo;

// Lista creata per contenere gli attributi delle classi
public class ListaAttributiClasse extends LinkedList<Campo> {

	private static final long serialVersionUID = 3416414813704607605L;

	@Override
	public String toString() {
		String serialization = "[";
		StringBuilder builder = new StringBuilder(serialization);
		for(Campo item : this) {
			
			builder.append(item.toString());
			builder.append(",");
		}
		
		return builder.substring(0, builder.length()-1).concat("]");
	}
	
}
