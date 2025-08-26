package com.univade.TU.generator.service;

import com.univade.TU.generator.exception.TestGenerationException;
import com.univade.TU.generator.model.EntityMetaModel;
import com.univade.TU.generator.model.GeneratedTestResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class FileWriterService {

    private static final String BASE_TEST_PATH = "src/test/java";
    private static final String BASE_BUILDER_PATH = "src/test/java";
    private static final String BASE_MAIN_PATH = "src/main/java";

    public void writeGeneratedFiles(EntityMetaModel entityMetaModel, GeneratedTestResult result) {
        try {
            String basePackage = entityMetaModel.getPackageName().replace(".entity", "");
            String basePackagePath = basePackage.replace(".", "/");
            String entityName = entityMetaModel.getName();

            if (result.getRepositoryTestContent() != null) {
                String testPath = basePackagePath + "/repository";
                writeFile(BASE_TEST_PATH + "/" + testPath,
                         entityName + "RepositoryTest.java",
                         result.getRepositoryTestContent());
            }

            if (result.getServiceTestContent() != null) {
                String testPath = basePackagePath + "/service";
                writeFile(BASE_TEST_PATH + "/" + testPath,
                         entityName + "ServiceTest.java",
                         result.getServiceTestContent());
            }

            if (result.getControllerTestContent() != null) {
                String testPath = basePackagePath + "/controller";
                writeFile(BASE_TEST_PATH + "/" + testPath,
                         entityName + "ControllerTest.java",
                         result.getControllerTestContent());
            }

            if (result.getBuilderContent() != null) {
                String builderPath = basePackagePath + "/testdata";
                writeFile(BASE_BUILDER_PATH + "/" + builderPath,
                         entityName + "TestDataBuilder.java",
                         result.getBuilderContent());
            }

            if (result.getValidationTestContent() != null) {
                String testPath = basePackagePath.replace("/entity", "/validation");
                writeFile(BASE_TEST_PATH + "/" + testPath,
                         entityName + "ValidationTest.java",
                         result.getValidationTestContent());
            }

            if (result.getDtoContent() != null) {
                String dtoPath = basePackagePath + "/dto";
                writeFile(BASE_MAIN_PATH + "/" + dtoPath,
                         entityName + "Dto.java",
                         result.getDtoContent());
            }

            if (result.getSecurityConfigContent() != null) {
                String configPath = basePackagePath + "/config";
                writeFile(BASE_TEST_PATH + "/" + configPath,
                         "TestSecurityConfig.java",
                         result.getSecurityConfigContent());
            }

            if (result.getCrudTestContent() != null) {
                String testPath = basePackagePath.replace("/entity", "/crud");
                writeFile(BASE_TEST_PATH + "/" + testPath,
                         entityName + "CrudTest.java",
                         result.getCrudTestContent());
            }

            if (result.getRelationshipTestContent() != null) {
                String testPath = basePackagePath.replace("/entity", "/relationship");
                writeFile(BASE_TEST_PATH + "/" + testPath,
                         entityName + "RelationshipTest.java",
                         result.getRelationshipTestContent());
            }

        } catch (Exception e) {
            throw TestGenerationException.fileWriteError("generated test files", e);
        }
    }

    private void writeFile(String directoryPath, String fileName, String content) throws IOException {
        Path directory = Paths.get(directoryPath);
        Files.createDirectories(directory);
        
        Path filePath = directory.resolve(fileName);
        Files.write(filePath, content.getBytes(), 
                   StandardOpenOption.CREATE, 
                   StandardOpenOption.TRUNCATE_EXISTING);
        
        System.out.println("Generated file: " + filePath.toAbsolutePath());
    }

    public String getGeneratedFilesLocation() {
        return "Generated test files are saved in the 'src/test/java' directory:\n\n" +
               "src/test/java/\n" +
               "  └── com/univade/TU/\n" +
               "      ├── entity/repository/  Repository tests (*RepositoryTest.java)\n" +
               "      └── entity/testdata/    Test data builders (*TestDataBuilder.java)\n\n" +
               "All files are ready to use in your project.";
    }
}
