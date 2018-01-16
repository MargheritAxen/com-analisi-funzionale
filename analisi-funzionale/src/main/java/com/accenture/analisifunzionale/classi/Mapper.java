package com.accenture.analisifunzionale.classi;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

	private static final int MAPPER_FILE_NAME_COLUMN = 6;
	private static final int NOME_MAPPER_COLUMN = 7;
	private static final String MAPPING_MANCANTI_MODEL_ENTITY_SHEETNAME = "MancantiME";
	private static final String MAPPING_MANCANTI_ENTITY_MODEL_SHEETNAME = "MancantiEM";
	private static final String ARGB_RED = "FF0000";
	private static final String ARGB_BLUE = "0000FF";
	private static final String ARGB_WHITE = "FFFFFF";
	private static final String ARGB_YELLOW = "FFFF00";
	private static final int MAPPING_START_OFFSET = 0;
	private static final int HEADER_ROW_OFFSET = 0;

	private static final String EXPLICIT_MAPS_SHEETNAME = "explicitMaps";
	private List<Mapping> mappingEspliciti = new ArrayList<>();
	private List<Mapping> mappingImpliciti = new ArrayList<>();
	private List<Mapping> mappingMancantiFrom = new ArrayList<>();
	private List<Mapping> mappingMancantiTo = new ArrayList<>();

	private String nomeMapper;
	private String fileMapper;
	private String classeFrom;
	private String classeTo;
	private boolean hasByDefault;
	private boolean hasCustom;
	private boolean isMapperErrato;

	public Mapper() {
	}

	public Mapper(String nomeMapper) {
		this.nomeMapper = nomeMapper;
	}

	public Mapper(List<Mapping> mappingEspliciti, List<Mapping> mappingImpliciti, List<Mapping> mappingMancantiFrom,
			List<Mapping> mappingMancantiTo, String nomeMapper, String fileMapper, String classeFrom, String classeTo,
			boolean hasByDefault, boolean hasCustom) {
		super();
		this.mappingEspliciti = mappingEspliciti;
		this.mappingImpliciti = mappingImpliciti;
		this.mappingMancantiFrom = mappingMancantiFrom;
		this.mappingMancantiTo = mappingMancantiTo;
		this.nomeMapper = nomeMapper;
		this.fileMapper = fileMapper;
		this.classeFrom = classeFrom;
		this.classeTo = classeTo;
		this.hasByDefault = hasByDefault;
		this.hasCustom = hasCustom;
	}

	public Mapper(String fileMapper, String nomeMapper) {
		this.nomeMapper = nomeMapper;
		this.fileMapper = fileMapper;
	}

	// toString
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Mapper [mappingEspliciti = ");
		builder.append(mappingEspliciti);
		builder.append(", mappingImpliciti = ");
		builder.append(mappingImpliciti);
		builder.append(", mappingMancantiFrom = ");
		builder.append(mappingMancantiFrom);
		builder.append(", mappingMancantiTo = ");
		builder.append(mappingMancantiTo);
		builder.append(", nomeMapper = ");
		builder.append(nomeMapper);
		builder.append(", fileMapper = ");
		builder.append(fileMapper);
		builder.append(", classeFrom = ");
		builder.append(classeFrom);
		builder.append(", classeTo = ");
		builder.append(classeTo);
		builder.append(", hasByDefault = ");
		builder.append(hasByDefault);
		builder.append(", hasCustom = ");
		builder.append(hasCustom);
		builder.append("]");
		return builder.toString();
	}

	// Recupero il nome del file dal file excel
	public static List<Mapper> recuperaMappers(Sheet mappersSheet) throws IOException {
		ArrayList<Mapper> listaMappers = new ArrayList<>();

		for (int i = 1; i <= mappersSheet.getLastRowNum(); i++) {
			Row mapperRows = mappersSheet.getRow(i);
			Cell cellFileName = mapperRows.getCell(0);
			Cell cellClassName = mapperRows.getCell(1);
			listaMappers.add(new Mapper(cellFileName.toString(), cellClassName.toString()));
		}

		return listaMappers;
	}

	// Popolo il Mapper dal file excel
	public static void popolaMapper(Sheet mappersSheet, Mapper mapper, List<Classe> models, List<Classe> entities) {

		for (int i = 1; i <= mappersSheet.getLastRowNum(); i++) {

			Row fileRow = mappersSheet.getRow(i);
			Cell nomeFile = fileRow.getCell(0);
			Cell nomeClasse = fileRow.getCell(1);
			Cell classeModello = fileRow.getCell(2);
			Cell classeEntity = fileRow.getCell(3);
			Cell tipoMapping = fileRow.getCell(4);
			Cell nomeCampoModel = fileRow.getCell(5);
			Cell nomeCampoEntity = fileRow.getCell(6);
			if (nomeFile == null || nomeClasse == null || classeModello == null || classeEntity == null
					|| tipoMapping == null) {
				System.out.println("Vuoto!");
				System.out.println("Riga :" + String.valueOf(i));
				throw new RuntimeException("Vuoto!");
			}
			if (mapper.getNomeMapper().equals(nomeClasse.toString())) {
				mapper.setClasseFrom(classeModello.toString());
				mapper.setClasseTo(classeEntity.toString());

				aggiungiMappingMapper(mapper, models, entities, classeModello, classeEntity, tipoMapping,
						nomeCampoModel, nomeCampoEntity);
			}
		}
		// Se il mapping è errato usciamo a monte altrimenti ci riporta l'errore e non
		// ci termina il programma
		if (mapper.isMapperErrato) {
			return;
		}
		String stringCellValueClasseModello = mapper.classeFrom;
		String stringCellValueClasseEntity = mapper.classeTo;

		Classe model = models.stream().filter(modello -> (modello.getNome().equals(mapper.getClasseFrom()))).findFirst()
				.orElse(null);
		Classe entity = entities.stream().filter(entita -> (entita.getNome().equals(mapper.getClasseTo()))).findFirst()
				.orElse(null);

		// Associazioni esplicite
		List<AssociazioneCampi> associazioniEsplicite = mapper.getMappingEspliciti().stream()
				.map(Mapping::getAssociazioneCampi).collect(Collectors.toList());
		if (mapper.hasByDefault) {
			for (Campo campo : model.getListaCampi()) {
				Campo mappingTo = entity.getListaCampi().stream().filter(campoE -> campoE.omonimous(campo)).findFirst()
						.orElse(null);
				if (mappingTo != null && associazioniEsplicite.stream()
						.filter(ass -> (ass.getCampoFrom().equals(campo))).count() == 0) {
					AssociazioneCampi associazioneCampiImplicita = new AssociazioneCampi();
					associazioneCampiImplicita.setCampoFrom(campo);
					associazioneCampiImplicita.setCampoTo(mappingTo);

					Mapping mapping = new Mapping(model.getNome(), entity.getNome(), "byDefault");
					mapping.setAssociazioneCampi(associazioneCampiImplicita);
					mapper.getMappingImpliciti().add(mapping);
				}
			}
		}
		// Associazioni campi impliciti e mancanti To -> model
		List<AssociazioneCampi> associazioniImplicite = mapper.getMappingImpliciti().stream()
				.map(Mapping::getAssociazioneCampi).collect(Collectors.toList());
		List<AssociazioneCampi> mappingMancantiTo = associazioniCampiMancantiTo(model, entity);
		List<Campo> assImplTo = associazioniImplicite.stream().map(AssociazioneCampi::getCampoTo)
				.collect(Collectors.toList());
		List<Campo> assEsplTo = associazioniEsplicite.stream().map(AssociazioneCampi::getCampoTo)
				.collect(Collectors.toList());

		mappingMancantiTo = mappingMancantiTo.stream().filter(
				mapping -> (!assImplTo.contains(mapping.getCampoTo()) && !assEsplTo.contains(mapping.getCampoTo())))
				.collect(Collectors.toList());

		for (AssociazioneCampi mappingMancante : mappingMancantiTo) {

			Mapping mapping = new Mapping(stringCellValueClasseModello, stringCellValueClasseEntity, "");

			mapping.setAssociazioneCampi(mappingMancante);
			mapper.mappingMancantiTo.add(mapping);

		}

		// Associazione mancanti From -> entity
		List<Campo> assImplFrom = associazioniImplicite.stream().map(AssociazioneCampi::getCampoFrom)
				.collect(Collectors.toList());
		List<Campo> assEsplFrom = associazioniEsplicite.stream().map(AssociazioneCampi::getCampoFrom)
				.collect(Collectors.toList());

		List<AssociazioneCampi> mappingMancantiFrom = associazioniCampiMancantiFrom(model, entity);
		mappingMancantiFrom = mappingMancantiFrom.stream()
				.filter(mapping -> (!assImplFrom.contains(mapping.getCampoFrom())
						&& !assEsplFrom.contains(mapping.getCampoFrom())))
				.collect(Collectors.toList());

		for (AssociazioneCampi mappingMancante : mappingMancantiFrom) {

			Mapping mapping = new Mapping(stringCellValueClasseModello, stringCellValueClasseEntity, "");

			mapping.setAssociazioneCampi(mappingMancante);
			mapper.mappingMancantiFrom.add(mapping);
		}
	}

	// Lista per inserimento dei campi mancanti To -> model
	private static List<AssociazioneCampi> associazioniCampiMancantiTo(Classe model, Classe entity) {
		return entity.getListaCampi().stream()
				.filter(campo -> (model.getListaCampi().stream().noneMatch(campoM -> (campoM.omonimous(campo)))))
				.map(campo -> {
					AssociazioneCampi ass = new AssociazioneCampi();
					ass.setCampoTo(campo);
					return ass;
				}).collect(Collectors.toList());
	}

	// Lista per inserimento dei campi mancanti From -> entity
	private static List<AssociazioneCampi> associazioniCampiMancantiFrom(Classe model, Classe entity) {
		return model.getListaCampi().stream()
				.filter(campo -> (entity.getListaCampi().stream().noneMatch(campo::omonimous))).map(campo -> {
					AssociazioneCampi ass = new AssociazioneCampi();
					ass.setCampoFrom(campo);
					return ass;
				}).collect(Collectors.toList());
	}

	// Metodo per aggiungere i mapping presenti nella tabella Excel che fa gli
	// opportuni contolli affinché vengano popolate le liste corrette dei vari
	// mapping differenti
	private static void aggiungiMappingMapper(Mapper mapper, List<Classe> models, List<Classe> entities,
			Cell classeModello, Cell classeEntity, Cell tipoMapping, Cell nomeCampoModel, Cell nomeCampoEntity) {

		String stringCellValueClasseModello = classeModello.toString().trim();
		String stringCellValueClasseEntity = classeEntity.toString().trim();
		String stringCellValueTipoMapping = tipoMapping.toString().trim();

		Classe model = models.stream().filter(modello -> (modello.getNome().equals(mapper.getClasseFrom()))).findFirst()
				.orElse(null);
		Classe entity = entities.stream().filter(entita -> (entita.getNome().equals(mapper.getClasseTo()))).findFirst()
				.orElse(null);
		if (model == null || entity == null) {
			System.out.println("mapping esplicito errato:".concat(mapper.getNomeMapper()).concat("\tfrom: ")
					.concat(mapper.getClasseFrom()).concat(" to: ").concat(mapper.getClasseTo()));
			mapper.setMapperErrato(true);
			return;
		}
		if ("field".equals(stringCellValueTipoMapping) || "fieldMap".equals(stringCellValueTipoMapping)) {

			Mapping mapping = new Mapping(stringCellValueClasseModello, stringCellValueClasseEntity,
					stringCellValueTipoMapping);

			String nomeCampoExcelModel = nomeCampoModel.toString();
			Campo campoModel = model.getListaCampi().stream()
					.filter(campo -> (nomeCampoExcelModel.equals(campo.getNome()))).findFirst().orElse(null);
			String nomeCampoExcelEntity = nomeCampoEntity.toString();
			Campo campoEntity = entity.getListaCampi().stream()
					.filter(campo -> (nomeCampoExcelEntity.equals(campo.getNome()))).findFirst().orElse(null);
			if (campoModel == null || campoEntity == null) {
				String campoFrom = campoModel == null ? nomeCampoExcelModel : campoModel.getNome();
				String campoTo = campoEntity == null ? nomeCampoExcelEntity : campoEntity.getNome();
				System.out.println("mapping esplicito errato:".concat(mapper.getNomeMapper()).concat("\tfrom: ")
						.concat(campoFrom).concat(" to: ").concat(campoTo));
			} else {
				AssociazioneCampi associazioneCampi = new AssociazioneCampi();
				associazioneCampi.setCampoFrom(campoModel);
				associazioneCampi.setCampoTo(campoEntity);
				mapper.getMappingEspliciti().add(mapping);
				mapping.setAssociazioneCampi(associazioneCampi);
			}
		}
		if ("byDefault".equals(stringCellValueTipoMapping)) {
			mapper.setHasByDefault(true);
		}
		if ("customize".equals(stringCellValueTipoMapping)) {
			mapper.setHasCustom(true);
		}
	}

	// Metodo che viene richiamato dal metodo aggiungiMappingMapper al fine di
	// settare il booleano a true in caso il tipo di mapping esplicito sia errato
	private void setMapperErrato(boolean esisteMapperErrato) {
		this.isMapperErrato = esisteMapperErrato;

	}

	// Metodo che crea un nuovo sheet per inserire i vari mapping ricavati ->
	// workbook espliciti
	public int toXslx1(XSSFWorkbook workBook, int offset) {
		Sheet sheet = workBook.getSheet(EXPLICIT_MAPS_SHEETNAME);
		int writtenLines = 0;
		if (sheet == null) {
			sheet = workBook.createSheet(EXPLICIT_MAPS_SHEETNAME);
			this.makeHeaderWithOffset(HEADER_ROW_OFFSET, sheet);
			++writtenLines;
		}

		writtenLines += serializzaMappingEspliciti(HEADER_ROW_OFFSET + writtenLines + offset, sheet);
		for (int columnIndex = 0; columnIndex < 10; columnIndex++) {
			sheet.autoSizeColumn(columnIndex);
		}
		if (this.hasByDefault) {
			writtenLines += serializzaMappingImpliciti(HEADER_ROW_OFFSET + writtenLines + offset, sheet);
			for (int columnIndex = 0; columnIndex < 10; columnIndex++) {
				sheet.autoSizeColumn(columnIndex);
			}
		}
		return writtenLines;
	}

	// Creiamo i metodi separati per creare workbook differenti: workBook Mancanti
	// ME
	public int toXsls2(XSSFWorkbook workBook, int offset) {
		int writtenLines = 0;
		if (!CollectionUtils.isEmpty(mappingMancantiFrom)) {
			writtenLines += serializzaMappingMancantiModelEntity(workBook, HEADER_ROW_OFFSET + writtenLines + offset);
		}
		return writtenLines;
	}

	// workBook Mancanti EM
	public int toXsls3(XSSFWorkbook workBook, int offset) {
		int writtenLines = 0;
		if (!CollectionUtils.isEmpty(mappingMancantiTo)) {
			writtenLines += serializzaMappingMancantiEntityModel(workBook, HEADER_ROW_OFFSET + writtenLines + offset);
		}
		return writtenLines;
	}

	// Metodo per inserire i mapping mancanti dall'Entity al Model nelle rispettive
	// e corrette liste
	private int serializzaMappingMancantiEntityModel(XSSFWorkbook workBook, int offset) {
		XSSFSheet missingEntityToModelMaps = workBook.getSheet(MAPPING_MANCANTI_ENTITY_MODEL_SHEETNAME);
		int numeroRighe = 0;
		if (missingEntityToModelMaps == null) {
			missingEntityToModelMaps = workBook.createSheet(MAPPING_MANCANTI_ENTITY_MODEL_SHEETNAME);
			this.makeHeaderWithOffset(HEADER_ROW_OFFSET, missingEntityToModelMaps);
			++numeroRighe;
		}
		for (int columnIndex = 0; columnIndex < 10; columnIndex++) {
			missingEntityToModelMaps.autoSizeColumn(columnIndex);
		}
		
		for (Mapping map : mappingMancantiTo) {
			Row mappingRow = missingEntityToModelMaps.createRow(offset+numeroRighe++);
			// Se ha custom lo colori di giallo, altrimenti rosso
			Color cellColor = this.hasCustom ? Color.YELLOW : Color.RED;
			XSSFCellStyle backGroundColourCellStyle = makeColouredBGStyle(mappingRow, cellColor);
			addFieldAtOffset(NOME_MAPPER_COLUMN, nomeMapper, mappingRow).setCellStyle(backGroundColourCellStyle);
			addFieldAtOffset(MAPPER_FILE_NAME_COLUMN, fileMapper, mappingRow).setCellStyle(backGroundColourCellStyle);
			map.serializeToRowOffsetWithStyle(MAPPING_START_OFFSET, mappingRow, backGroundColourCellStyle);
		}
		return numeroRighe;
	}

	// Stesso metodo precedente ma non più per da Entity a Model bensì dal Model
	// all'Entity
	private int serializzaMappingMancantiModelEntity(XSSFWorkbook workBook, int offset) {
		XSSFSheet missingModelToEntityMaps = workBook.getSheet(MAPPING_MANCANTI_MODEL_ENTITY_SHEETNAME);
		int numeroRighe = 0;
		if (missingModelToEntityMaps == null) {
			missingModelToEntityMaps = workBook.createSheet(MAPPING_MANCANTI_MODEL_ENTITY_SHEETNAME);
			this.makeHeaderWithOffset(HEADER_ROW_OFFSET, missingModelToEntityMaps);
			++numeroRighe;
		}
		for (int columnIndex = 0; columnIndex <= 10; columnIndex++) {
			missingModelToEntityMaps.autoSizeColumn(columnIndex);
		}
		
		for (Mapping map : mappingMancantiFrom) {
			Row mappingRow = missingModelToEntityMaps.createRow(offset + numeroRighe++);
			// Se ha custom lo colori di giallo, altrimenti rosso
			Color cellColor = this.hasCustom ? Color.YELLOW : Color.RED;
			XSSFCellStyle backGroundColourCellStyle = makeColouredBGStyle(mappingRow, cellColor);
			addFieldAtOffset(NOME_MAPPER_COLUMN, nomeMapper, mappingRow).setCellStyle(backGroundColourCellStyle);
			addFieldAtOffset(MAPPER_FILE_NAME_COLUMN, fileMapper, mappingRow).setCellStyle(backGroundColourCellStyle);
			map.serializeToRowOffsetWithStyle(MAPPING_START_OFFSET, mappingRow, backGroundColourCellStyle);
		}
		return numeroRighe;
	}

	// Metodo che viene richiamato quando il mapping è byDefault. Se ha al suo
	// interno anche il Custom
	// allora entra in tale metodo e viene cambiato il colore della cella in GIALLO
	// al fine di poterli
	// distinguere tra loro
	private int serializzaMappingImpliciti(int offset, Sheet sheet) {
		int numeroRighe = 0;
		for (Mapping map : this.mappingImpliciti) {
			Row mappingRow = sheet.createRow(offset + numeroRighe++);
			XSSFCellStyle backGroundColourCellStyle = null;
			// Se ha custom lo colori di giallo, altrimenti verde
			Color cellColor = this.hasCustom ? Color.YELLOW : Color.GREEN;
			backGroundColourCellStyle = makeColouredBGStyle(mappingRow, cellColor);
			addFieldAtOffset(MAPPER_FILE_NAME_COLUMN, fileMapper, mappingRow).setCellStyle(backGroundColourCellStyle);
			addFieldAtOffset(NOME_MAPPER_COLUMN, nomeMapper, mappingRow).setCellStyle(backGroundColourCellStyle);
			map.serializeToRowOffsetWithStyle(MAPPING_START_OFFSET, mappingRow, backGroundColourCellStyle);
		}
		return numeroRighe;
	}

	// Metodo che inizializza l'OFFSET con il Nome del Mapper e il file del Mapper
	// andando
	// a creare una opportuna cella in cui inserirli
	private int serializzaMappingEspliciti(int offset, Sheet sheet) {
		int numeroRighe = offset;
		for (Mapping map : this.mappingEspliciti) {
			Row mappingRow = sheet.createRow(numeroRighe++);
			addFieldAtOffset(MAPPER_FILE_NAME_COLUMN, fileMapper, mappingRow);
			addFieldAtOffset(NOME_MAPPER_COLUMN, nomeMapper, mappingRow);
			map.serializeToRowOffset(MAPPING_START_OFFSET, mappingRow);
		}
		return numeroRighe - offset;
	}

	// Metodo per settare il colore e lo stile del backgroud di excel e delle celle
	private XSSFCellStyle makeColouredBGStyle(Row mappingRow, Color argbColour) {
		XSSFCellStyle backGroundColourCellStyle;
		backGroundColourCellStyle = Mapping.buildForeGroundColourCellStyle(mappingRow, argbColour);
		return backGroundColourCellStyle;
	}

	// Aggiungiamo un campo all'OFFSET
	private Cell addFieldAtOffset(int offset, String field, Row mappingRow) {
		Cell createdCell = mappingRow.createCell(offset, CellType.STRING);
		createdCell.setCellValue(field);
		return createdCell;
	}

	// Creare l'intestazione con l'OFFSET -> creaiamo la prima riga dell'excel con
	// il
	// Nome del Mapper e del Path (percorso del file)
	private Row makeHeaderWithOffset(int headerOffset, Sheet sheet) {
		Row headerRow = sheet.createRow(headerOffset);
		XSSFCellStyle style = makeColouredBGStyle(headerRow, Color.CYAN);
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		new Mapping().serializeHeaderWithStyleToRowOffset(MAPPING_START_OFFSET, headerRow, style);
		this.addFieldAtOffset(NOME_MAPPER_COLUMN, "NomeMapper", headerRow).setCellStyle(style);
		this.addFieldAtOffset(MAPPER_FILE_NAME_COLUMN, "MapperPath", headerRow).setCellStyle(style);
		return headerRow;
	}

	// GETTERS e SETTERS
	public String getNomeMapper() {
		return nomeMapper;
	}

	public void setNomeMapper(String nomeMapper) {
		this.nomeMapper = nomeMapper;
	}

	public String getFileMapper() {
		return fileMapper;
	}

	public void setFileMapper(String fileMapper) {
		this.fileMapper = fileMapper;
	}

	public String getClasseFrom() {
		return classeFrom;
	}

	public void setClasseFrom(String classeFrom) {
		this.classeFrom = classeFrom;
	}

	public String getClasseTo() {
		return classeTo;
	}

	public void setClasseTo(String classeTo) {
		this.classeTo = classeTo;
	}

	public List<Mapping> getMappingEspliciti() {
		return mappingEspliciti;
	}

	public void setMappingEspliciti(List<Mapping> mappingEspliciti) {
		this.mappingEspliciti = mappingEspliciti;
	}

	public List<Mapping> getMappingImpliciti() {
		return mappingImpliciti;
	}

	public void setMappingImpliciti(List<Mapping> mappingImpliciti) {
		this.mappingImpliciti = mappingImpliciti;
	}

	public List<Mapping> getMappingMancantiFrom() {
		return mappingMancantiFrom;
	}

	public void setMappingMancantiFrom(List<Mapping> mappingMancantiFrom) {
		this.mappingMancantiFrom = mappingMancantiFrom;
	}

	public List<Mapping> getMappingMancantiTo() {
		return mappingMancantiTo;
	}

	public void setMappingMancantiTo(List<Mapping> mappingMancantiTo) {
		this.mappingMancantiTo = mappingMancantiTo;
	}

	public boolean hasByDefault() {
		return hasByDefault;
	}

	public void setHasByDefault(boolean hasByDefault) {
		this.hasByDefault = hasByDefault;
	}

	public boolean hasCustom() {
		return hasCustom;
	}

	public void setHasCustom(boolean hasCustom) {
		this.hasCustom = hasCustom;
	}

	public static int getMapperFileNameColumn() {
		return MAPPER_FILE_NAME_COLUMN;
	}

	public static int getNomeMapperColumn() {
		return NOME_MAPPER_COLUMN;
	}

	public static String getMappingMancantiModelEntitySheetname() {
		return MAPPING_MANCANTI_MODEL_ENTITY_SHEETNAME;
	}

	public static String getMappingMancantiEntityModelSheetname() {
		return MAPPING_MANCANTI_ENTITY_MODEL_SHEETNAME;
	}

	public static String getArgbRed() {
		return ARGB_RED;
	}

	public static String getArgbBlue() {
		return ARGB_BLUE;
	}

	public static String getArgbWhite() {
		return ARGB_WHITE;
	}

	public static String getArgbYellow() {
		return ARGB_YELLOW;
	}

	public static int getMappingStartOffset() {
		return MAPPING_START_OFFSET;
	}

	public static int getHeaderRowOffset() {
		return HEADER_ROW_OFFSET;
	}

	public static String getExplicitMapsSheetname() {
		return EXPLICIT_MAPS_SHEETNAME;
	}

	public boolean isHasByDefault() {
		return hasByDefault;
	}

	public boolean isHasCustom() {
		return hasCustom;
	}

	public boolean isMapperErrato() {
		return isMapperErrato;
	}
}
