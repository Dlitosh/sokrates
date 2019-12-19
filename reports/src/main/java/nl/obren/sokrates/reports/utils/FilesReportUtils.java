/*
 * Copyright (c) 2019 Željko Obrenović. All rights reserved.
 */

package nl.obren.sokrates.reports.utils;

import nl.obren.sokrates.sourcecode.SourceFile;

import java.io.File;
import java.util.List;

public class FilesReportUtils {

    public static String getFilesTable(List<SourceFile> sourceFiles, boolean linkToFiles) {
        StringBuilder table = new StringBuilder();

        table.append("<table style='width: 80%'>\n");
        table.append("<th>File</th><th># lines</th>\n");

        sourceFiles.forEach(sourceFile -> {
            table.append("<tr>\n");

            File file = new File(sourceFile.getRelativePath());

            String fileNameFragment;

            if (linkToFiles) {
                String href = "../src/main/" + sourceFile.getRelativePath();
                fileNameFragment = "<a target='blank' href='" + href + ".html'>" + file.getName() + "</a>";
            } else {
                fileNameFragment = file.getName();
            }

            table.append("<td><b>"
                    + fileNameFragment + "</b><br/>in " + file.getParent() + "<br/>" +
                    "</td>\n");
            table.append("<td>" + sourceFile.getLinesOfCode() + "</td>\n");

            table.append("</tr>\n");
        });

        table.append("</table>\n");

        return table.toString();
    }
}
