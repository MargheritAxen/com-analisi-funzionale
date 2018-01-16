package com.accenture.analisifunzionale.classi;

import java.awt.Color;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Mapping {

	// Dichiarazione delle variabili
	private String classeFrom;
	private String classeTo;
	private String tipoMapping;
	private AssociazioneCampi associazioneCampi;

	public Mapping() {
	}

	public Mapping(String classeFrom, String classeTo, String tipoCampo) {
		super();
		this.classeFrom = classeFrom;
		this.classeTo = classeTo;
		this.tipoMapping = tipoCampo;
	}

	// GETTERS e SETTERS
	public AssociazioneCampi getAssociazioneCampi() {
		return associazioneCampi;
	}

	public void setAssociazioneCampi(AssociazioneCampi associazioneCampi) {
		this.associazioneCampi = associazioneCampi;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("classeFrom = ");
		builder.append(classeFrom);
		builder.append(", classeTo = ");
		builder.append(classeTo);
		builder.append(", tipoMapping = ");
		builder.append(tipoMapping);
		return builder.toString();
	}

	// Serializza l'intestazione con lo stile delle righe dell'OFFSET
	public Row serializeHeaderWithStyleToRowOffset(int cellOffset, Row mappingRow, XSSFCellStyle cellStyle) {
		int cellOffsetLocalVar = cellOffset;
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, "ClasseFrom").setCellStyle(cellStyle);
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, "ClasseTo").setCellStyle(cellStyle);
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, "NomeAttributoFrom").setCellStyle(cellStyle);
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, "TipoAttributoFrom").setCellStyle(cellStyle);
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, "NomeAttributoTo").setCellStyle(cellStyle);
		addCellAtOffset(cellOffsetLocalVar, mappingRow, "TipoAttributoTo").setCellStyle(cellStyle);

		return mappingRow;

	}

	// Serializziamo sulle righe dell'OFFSET
	public Row serializeToRowOffset(int cellOffset, Row mappingRow) {
		int cellOffsetLocalVar = cellOffset;
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, this.getClasseFrom());
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, this.getClasseTo());

		addCellAtOffset(cellOffsetLocalVar++, mappingRow, this.associazioneCampi.getCampoFrom().getNome());
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, this.associazioneCampi.getCampoFrom().getTipo());
		addCellAtOffset(cellOffsetLocalVar++, mappingRow, this.associazioneCampi.getCampoTo().getNome());
		addCellAtOffset(cellOffsetLocalVar, mappingRow, this.associazioneCampi.getCampoTo().getTipo());

		return mappingRow;
	}

	// Serializziamo le righe dell'OFFSET con i colori
	public Row serializeToRowOffsetWithColour(int rowOffset, Row mappingRow, Color warningColor) {
		XSSFCellStyle backGroundColourCellStyle = buildForeGroundColourCellStyle(mappingRow, warningColor);
		serializeToRowOffsetWithStyle(rowOffset, mappingRow, backGroundColourCellStyle);
		return mappingRow;
	}

	// Aggiungiamo una cella colorata all'OFFSET
	private Cell addColouredCellAtOffset(int columnOffset, Row mappingRow, String value,
			XSSFCellStyle backGroundColourCellStyle) {
		Cell createdCell = addCellAtOffset(columnOffset, mappingRow, value);
		createdCell.setCellStyle(backGroundColourCellStyle);
		return createdCell;
	}

	// Aggiungiamo una cella all'OFFSET
	private Cell addCellAtOffset(int cellOffset, Row mappingRow, String value) {
		Cell createdCell = mappingRow.createCell(cellOffset, CellType.STRING);
		createdCell.setCellValue(value);
		return createdCell;
	}

	// Costruisci lo stile del Background della cella a colori
	public static XSSFCellStyle buildForeGroundColourCellStyle(Row mappingRow, Color color) {
		XSSFCellStyle createCellStyle = ((XSSFWorkbook) mappingRow.getSheet().getWorkbook()).createCellStyle();
		createCellStyle.setFillForegroundColor(new XSSFColor(color));
		createCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// Bordi neri di excel
		createCellStyle.setBorderBottom(BorderStyle.MEDIUM);
		createCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		createCellStyle.setBorderLeft(BorderStyle.MEDIUM);
		createCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		createCellStyle.setBorderRight(BorderStyle.MEDIUM);
		createCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		createCellStyle.setBorderTop(BorderStyle.MEDIUM);
		createCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return createCellStyle;
	}

	// Serializzare sulle righe dell'OFFSET con lo stile
	// controllo che campo from e to non sia nulla prima di serializzare
	public Row serializeToRowOffsetWithStyle(int mappingStartOffset, Row mappingRow,
			XSSFCellStyle backGroundColourCellStyle) {
		int localOffset = mappingStartOffset;

		addColouredCellAtOffset(localOffset, mappingRow, this.getClasseFrom(), backGroundColourCellStyle);
		addColouredCellAtOffset(localOffset++, mappingRow, this.getClasseTo(), backGroundColourCellStyle);

		addColouredCellAtOffset(localOffset++, mappingRow,
				this.associazioneCampi.getCampoFrom() == null ? null : this.associazioneCampi.getCampoFrom().getNome(),
				backGroundColourCellStyle);
		addColouredCellAtOffset(localOffset++, mappingRow,
				this.associazioneCampi.getCampoFrom() == null ? null : this.associazioneCampi.getCampoFrom().getTipo(),
				backGroundColourCellStyle);
		
		addColouredCellAtOffset(localOffset++, mappingRow,
				this.associazioneCampi.getCampoTo() == null ? null : this.associazioneCampi.getCampoTo().getNome(),
				backGroundColourCellStyle);
		addColouredCellAtOffset(localOffset++, mappingRow,
				this.associazioneCampi.getCampoTo() == null ? null : this.associazioneCampi.getCampoTo().getTipo(),
				backGroundColourCellStyle);
		
		return mappingRow;

	}
}
