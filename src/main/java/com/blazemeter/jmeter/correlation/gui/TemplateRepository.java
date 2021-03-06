package com.blazemeter.jmeter.correlation.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateRepository {

  private static final Logger LOG = LoggerFactory.getLogger(TemplateRepository.class);

  private String templatesPath;

  public TemplateRepository(String templatesPath) {
    this.templatesPath = templatesPath;
  }

  public void addSiebelTemplate(String templateName, String pathTemplateResource,
      String descTemplateName, String nameTemplateXML) {
    createSiebelTemplate(templateName, pathTemplateResource);
    addTemplateDescription(descTemplateName, nameTemplateXML);
  }

  private void createSiebelTemplate(String templateName, String pathTemplateResource) {
    try {
      File dest = new File(templatesPath + templateName);
      if (!dest.exists()) {
        try (FileWriter fileWriter = new FileWriter(dest)) {
          fileWriter.write(getFileFromResources(pathTemplateResource));
        }
      }
    } catch (IOException e) {
      LOG.warn("Problem creating Siebel recording template {}", templateName, e);
    }
  }

  private String getFileFromResources(String fileName) throws IOException {
    InputStream inputStream = this.getClass().getResourceAsStream(fileName);
    return IOUtils.toString(inputStream, "UTF-8");
  }

  private void addTemplateDescription(String descTemplateName, String nameTemplatesXML) {
    try {
      String filePath = templatesPath + "/templates.xml";
      if (!checkIfTemplateAlreadyExists(filePath, nameTemplatesXML)) {
        List<String> replacedLines = new ArrayList<>();
        for (String line : Files.readAllLines(Paths.get(filePath))) {
          if (line.contains("</templates>")) {
            line = getFileFromResources(descTemplateName) + System.lineSeparator() + line;
          }
          replacedLines.add(line);
        }
        Files.write(Paths.get(filePath), replacedLines);
      }
    } catch (IOException e) {
      LOG.warn("Problem adding Siebel recording template description {}", descTemplateName,
          e);
    }
  }

  private boolean checkIfTemplateAlreadyExists(String filePath, String nameXML)
      throws IOException {
    try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
      return lines.anyMatch(line -> line.contains("<name>" + nameXML + "</name>"));
    }
  }

}
