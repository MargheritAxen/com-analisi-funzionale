package com.accenture.analisifunzionale.classi;

public class Campo {

	// Dichiarazione delle variabili
	private String nome;
	private String tipo;

	public Campo() {
	}

	public Campo(String name, String type) {
		this.setNome(name);
		this.setTipo(type);
	}

	// GETTERS e SETTERS
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	// toString
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append(" \"nome\" : \"");
		builder.append(this.getNome());
		builder.append("\", \"tipo\" : \"");
		builder.append(this.getTipo());
		builder.append("\"}");
		return builder.toString();
	}

	// Override del metodo equals che viene richiamato nel Mapper dal metodo
	// "aggiungiMappingMapper"
	@Override
	public boolean equals(Object obj) {

		if (super.equals(obj)) {
			return true;
		}
		if (obj instanceof Campo) {
			Campo other = (Campo) obj;
			return other.nome.equals(this.nome);
		}
		return false;
	}

	// Metodo che serve per ricavare l'omonimo nome della lista dei campi
	public boolean omonimous(Campo other) {
		if (other == null) {
			return false;
		}
		return other.nome.equals(this.nome) && other.tipo.equals(this.tipo);
	}

	// Override dell'hasCode richiesto da eclipse sul metodo precedente
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
