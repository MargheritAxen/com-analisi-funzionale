package com.accenture.analisifunzionale.classi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.accenture.analisifunzionale.classi.liste.ListaAttributiClasse;

public class Classe {

	// Dichiarazione delle variabili
	private String nome;
	private ListaAttributiClasse listaCampi = new ListaAttributiClasse();

	public Classe() {
	}

	public Classe(String nome) {
		super();
		this.nome = nome;
	}

	// GETTERS e SETTERS
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public ListaAttributiClasse getListaCampi() {
		return listaCampi;
	}

	public void setListaCampi(ListaAttributiClasse listaCampi) {
		this.listaCampi = listaCampi;
	}

	// toString -> JSON
	@Override
	public String toString() {
		String serialization = "{";
		serialization = serialization.concat("\"nomeClasse\" : \"").concat(this.getNome()).concat("\", \"campi\" : ")
				.concat(this.getListaCampi().toString()).concat("}");
		return serialization;
	}

	// Metodo per recuperare le classi dal foglio excel
	public static List<Classe> recuperaClassi(Sheet classesSheet) throws IOException {
		ArrayList<Cell> listaEntities = new ArrayList<>();

		for (int i = 1; i <= classesSheet.getLastRowNum(); i++) {
			Row entitiesRow = classesSheet.getRow(i);
			Cell cellEntities = entitiesRow.getCell(0);
			listaEntities.add(cellEntities);
		}

		return listaEntities.stream().filter(cell -> (cell.getCellTypeEnum() == CellType.STRING))
				.map(Cell::getStringCellValue).distinct().map(cell -> (new Classe(cell))).collect(Collectors.toList());

	}

	// Metodo per recuperare le i campi delle classi dal foglio excel
	public static void recuperaCampiClassi(Sheet classesSheet, List<Classe> classi) {
		Map<String, Classe> classesMap = classi.stream().collect(Collectors.toMap(elmK -> (elmK.getNome()), elm->{return elm;}));
		for (int i = 1; i <= classesSheet.getLastRowNum(); i++) {

			Row campiRow = classesSheet.getRow(i);
			Cell nomeCampo = campiRow.getCell(1);
			Cell tipoCampo = campiRow.getCell(2);
			if (nomeCampo == null) {
				System.out.println("Vuoto!");
				System.out.println("Riga :".concat( String.valueOf(i)));
				throw new RuntimeException("Vuoto!");

			}
			/*
			 * if (nomeCampo.getCellTypeEnum() == CellType.STRING) {
			 * 
			 * System.out.println("Incorrect cell type ".concat("nomeCampo"));
			 * System.out.println("Riga :" + String.valueOf(i)); throw new
			 * RuntimeException("Incorrect cell type!");
			 * 
			 * } if (tipoCampo.getCellTypeEnum() == CellType.STRING) {
			 * System.out.println("Incorrect cell type for ".concat("tipoCampo"));
			 * System.out.println("Riga :" + String.valueOf(i)); throw new
			 * RuntimeException("Incorrect cell type!");
			 * 
			 * }
			 */
			String stringCellValueNomeCampo = nomeCampo.toString().trim();
			String stringCellValueTipoCampo = tipoCampo.toString().trim();
			String nomeClasse = campiRow.getCell(0).getStringCellValue();
			Classe classeEstratta = classesMap.get(nomeClasse);
			if (classeEstratta != null) {
				classeEstratta.getListaCampi().add(new Campo(stringCellValueNomeCampo, stringCellValueTipoCampo));
			}
		}
	}
}