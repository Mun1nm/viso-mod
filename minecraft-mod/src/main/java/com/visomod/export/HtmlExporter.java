package com.visomod.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class HtmlExporter {

    public static File generateStandaloneHtml(File exportsDir, String fileName, String jsonContent) throws IOException {
        File htmlFile = new File(exportsDir, fileName + ".html");

        String template = loadViewerTemplate();
        if (template == null || template.isEmpty()) {
            throw new IOException("Template HTML do visualizador (/assets/visomod/viewer_template.html) não encontrado nos recursos do mod.");
        }

        // Replaces the placeholder inside <script id="embedded-structure-data" type="application/json">
        String generatedHtml = template.replace("<!-- EMBEDDED_STRUCTURE_DATA -->", jsonContent);

        try (FileOutputStream fos = new FileOutputStream(htmlFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.write(generatedHtml);
        }

        return htmlFile;
    }

    private static String loadViewerTemplate() throws IOException {
        try (InputStream is = HtmlExporter.class.getResourceAsStream("/assets/visomod/viewer_template.html")) {
            if (is == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        }
    }
}
