package com.accenture.analisifunzionale;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.accenture.analisifunzionale.classi.Classe;
import com.accenture.analisifunzionale.classi.Mapper;
import com.accenture.analisifunzionale.filehelper.FileHelper;

@SpringBootApplication
@EnableAutoConfiguration
public class AnalisiFunzionaleApplication {

	// Dichiarazione delle variabili
	private static final String MAPPERS_CRA_SHEET_NAME = "Mappers";
	// TODO: decommentare in seguito i due sheet relativi a GL
	// private static final String MODELS_GL_SHEET = "Model";
	// private static final String ENTITIES_GL_SHEET = "Entity";
	private static final String MODEL_CRA_SHEET = "Models";
	private static final String ENTITIES_CRA_SHEET = "Entities";
	// private static final int NOME_MAPPER_COLUMN = 1;
	// private static final String RISULTATO_CRA_SHEET = "Risultato";

	public static void main(String[] args) throws IOException, URISyntaxException {

		// Path relativo ai file excel che viene inserito nelle resources del progetto e
		// del repository
		// tramite i metodi sottostanti
		String excelDatiMapperCRA = "/cra/DatiMapper.xlsx";
		String excelAttributiEntitiesCRA = "/cra/ListaAttributiEntities.xlsx";
		String excelAttributiModelCRA = "/cra/ListaAttributiModel.xlsx";
		// TODO: decommentare in seguito gli excel non utilizzati
		// String excelAttributiEntitiesGL = "/gl/ListaAttributiEntity.xlsx";
		// String excelAttributiModelGL = "/gl/ListaAttributiModels.xlsx";
		// String excelDatiMapperRisultatoCRA = "/cra/DatiMapperRisultato.xlsx";

		System.out.println("---------------------------------------------------------------------------");
		System.out.println("Iniziato: recupero dati CRA");
		System.out.println("---------------------------------------------------------------------------");

		System.out.println("-------------------------------ENTITY CRA----------------------------------");
		System.out.println("---------------------------------------------------------------------------");

		FileHelper recuperaInput = new FileHelper();
		FileHelper restituisciOutput = new FileHelper();
		List<Classe> craModels = null;
		List<Classe> craEntities = null;
		// TODO: decommentare in seguito le liste GL non utilizzate ora
		// List<Classe> glModels = null;
		// List<Classe> glEntities = null;

		// Metodi per inizializzare ed aprire i file excel in modo autonomo tramite la
		// libreria WorkBook
		// Tramite la classe FileHelper rendiamo l'applicazione autoconsistente
		// aggiungendo
		// al buildpath l'input
		try (InputStream inputStream = recuperaInput.getResource(excelAttributiEntitiesCRA);) {
			try (Workbook workbook = new XSSFWorkbook(inputStream);) {
				Sheet entitiesSheet = workbook.getSheet(ENTITIES_CRA_SHEET);

				craEntities = Classe.recuperaClassi(entitiesSheet);
				Classe.recuperaCampiClassi(entitiesSheet, craEntities);
				for (Classe item : craEntities) {
					System.out.println(item.toString());
				}
			}
		}

		System.out.println("--------------------------------------------------------------------------");
		System.out.println("--------------------------MODEL CRA---------------------------------------");
		System.out.println("--------------------------------------------------------------------------");

		try (InputStream inputStream = recuperaInput.getResource(excelAttributiModelCRA);) {
			try (Workbook workbook = new XSSFWorkbook(inputStream);) {
				Sheet modelsSheet = workbook.getSheet(MODEL_CRA_SHEET);
				craModels = Classe.recuperaClassi(modelsSheet);
				Classe.recuperaCampiClassi(modelsSheet, craModels);
				for (Classe item : craModels) {
					System.out.println(item.toString());
				}
			}
		}
		System.out.println("----------------------------------------------------------------");
		System.out.println("Iniziato: il recupero dei mapping Model-Entity CRA");
		System.out.println("----------------------------------------------------------------");

		// Leggo i mapper dal file excel
		// String mappersSheetName = MAPPERS_CRA_SHEET_NAME;
		try (InputStream inputStream = recuperaInput.getResource(excelDatiMapperCRA);) {
			try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream);) {
				Sheet mappersSheet = workbook.getSheet(MAPPERS_CRA_SHEET_NAME);
				List<Mapper> mappers = Mapper.recuperaMappers(mappersSheet);

				// Crei un file di output, e gli passi come parametro l'excel in cui dobbiamo
				// andare a scrivere, dopo di ché va creato il file di scrittura prima di poter
				// scrivere il workbook correttamente
				for (Mapper mapper : mappers) {
					Mapper.popolaMapper(mappersSheet, mapper, craModels, craEntities);
				}

				try (XSSFWorkbook workbook1 = new XSSFWorkbook()) {
					int offset = 0;
					int offset1 = 0;
					int offset2 = 0;
					for (Mapper item : mappers) {
						offset += item.toXslx1(workbook1, offset);
						offset1 += item.toXsls2(workbook1, offset1);
						offset2 += item.toXsls3(workbook1, offset2);

					}
					File outputFile = new FileHelper().createFileOnBuildPath(args[0]);
					try (FileOutputStream outStream = new FileOutputStream(outputFile)) {
						workbook1.write(outStream);
					}

				}

				System.out.println("-------------------------------------------------------------------");
				System.out.println("Recupero mapper Model-Entity terminato: file scritto con successo!");
				System.out.println("-------------------------------------------------------------------");
			}
		}

		// TODO: decommentare per gl in un secondo momento.
		/*
		 * try (InputStream inputStream = helper.getResource(excelAttributiEntitiesGL);)
		 * { try (Workbook workbook = new XSSFWorkbook(inputStream);) { Sheet
		 * entitesSheet = workbook.getSheet(ENTITIES_GL_SHEET); glEntities =
		 * Classe.recuperaClassi(entitesSheet); Classe.recuperaCampiClassi(entitesSheet,
		 * glEntities); } } try (InputStream inputStream =
		 * helper.getResource(excelAttributiModelGL);) { try (Workbook workbook = new
		 * XSSFWorkbook(inputStream);) { Sheet modelsSheet =
		 * workbook.getSheet(MODELS_GL_SHEET); glModels =
		 * Classe.recuperaClassi(modelsSheet); Classe.recuperaCampiClassi(modelsSheet,
		 * glModels); } }
		 */

		System.out.println("----------------------------------------------------------------");
		System.out.println("Terminato: recupero Mapper Model-Entity CRA");
		System.out.println("----------------------------------------------------------------");
		System.out.println("----------------------------------------------------------------");

	}

	// // Proviamo a creare un metodo che aggiunga le celle
	// private static Cell addFieldAtOffset(int offset, String field, Row
	// mappingRow) {
	// Cell createdCell = mappingRow.createCell(offset, CellType.STRING);
	// createdCell.setCellValue(field);
	// return createdCell;
	// }
}
