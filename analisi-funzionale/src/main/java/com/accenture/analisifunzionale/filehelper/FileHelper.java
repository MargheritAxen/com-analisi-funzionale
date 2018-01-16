package com.accenture.analisifunzionale.filehelper;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

//Classe che serve per aggiungere al buildpath l'input rendendo l'applicazione autoconsistente
public class FileHelper {

	public InputStream getResource(String relativePath) {
		return this.getClass().getResourceAsStream(relativePath);
	}

	public File createFileOnBuildPath(String fileName) throws URISyntaxException {
		URL url = this.getClass().getResource("/cra");
		File parent = new File(new URI(url.toString()));
		return new File(parent, fileName);
	}

	public File createFile(String fileName) {
		return new File(fileName);
	}
}
