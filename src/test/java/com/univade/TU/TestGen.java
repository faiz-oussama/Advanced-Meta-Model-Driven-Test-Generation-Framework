package com.univade.TU;

import com.univade.TU.generator.model.EntityMetaModel;
import com.univade.TU.generator.model.GeneratedTestResult;
import com.univade.TU.generator.parser.EntityMetaModelParser;
import com.univade.TU.generator.service.RepositoryTestGenerator;
import com.univade.TU.generator.service.SecurityConfigGenerator;
import com.univade.TU.generator.service.ServiceTestGenerator;
import com.univade.TU.generator.service.ControllerTestGenerator;
import com.univade.TU.generator.service.FileWriterService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Disabled
class TestGen {

    private static final List<String> SAMPLE_JSON_FILES = List.of(
        "user-meta-model.json",
        "post-meta-model.json",
        "address-meta-model.json",
            "category-meta-model.json",
            "person-meta-model.json"
    );

    @Autowired
    private RepositoryTestGenerator repositoryTestGenerator;

    @Autowired
    private ServiceTestGenerator serviceTestGenerator;

    @Autowired
    private ControllerTestGenerator controllerTestGenerator;

    @Autowired
    private FileWriterService fileWriterService;

    @Autowired
    private SecurityConfigGenerator securityConfigGenerator;

    @Autowired
    private EntityMetaModelParser parser;

    @BeforeEach
    void setUp() {
        cleanupGeneratedFiles();
    }

    private void cleanupGeneratedFiles() {
        try {
            String baseTestPath = "src/test/java/com/univade/TU";

            Path repositoryDir = Paths.get(baseTestPath, "repository");
            if (Files.exists(repositoryDir)) {
                Files.walk(repositoryDir)
                    .filter(path -> path.toString().endsWith("RepositoryTest.java"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                        }
                    });
            }

            Path serviceDir = Paths.get(baseTestPath, "service");
            if (Files.exists(serviceDir)) {
                Files.walk(serviceDir)
                    .filter(path -> path.toString().endsWith("ServiceTest.java"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                        }
                    });
            }

            Path controllerDir = Paths.get(baseTestPath, "controller");
            if (Files.exists(controllerDir)) {
                Files.walk(controllerDir)
                    .filter(path -> path.toString().endsWith("ControllerTest.java"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                        }
                    });
            }

            Path testDataDir = Paths.get(baseTestPath, "testdata");
            if (Files.exists(testDataDir)) {
                Files.walk(testDataDir)
                    .filter(path -> path.toString().endsWith("TestDataBuilder.java"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                        }
                    });
            }
        } catch (IOException e) {
        }
    }

    @Test
    void shouldGenerateAllTestFilesFromSampleJson() throws IOException {
        List<EntityMetaModel> allEntities = new ArrayList<>();

        for (String sampleFile : SAMPLE_JSON_FILES) {
            ClassPathResource resource = new ClassPathResource("samples/" + sampleFile);
            String jsonContent = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()), StandardCharsets.UTF_8);

            EntityMetaModel entityMetaModel = parser.parseFromJson(jsonContent);
            
            if (!entityMetaModel.getPackageName().endsWith(".entity")) {
                entityMetaModel = EntityMetaModel.builder()
                    .name(entityMetaModel.getName())
                    .packageName(entityMetaModel.getPackageName() + ".entity")
                    .tableName(entityMetaModel.getTableName())
                    .auditable(entityMetaModel.isAuditable())
                    .attributes(entityMetaModel.getAttributes())
                    .relationships(entityMetaModel.getRelationships())
                    .validations(entityMetaModel.getValidations())
                    .securityRules(entityMetaModel.getSecurityRules())
                    .build();
            }

            allEntities.add(entityMetaModel);

            GeneratedTestResult repositoryResult = repositoryTestGenerator.generateRepositoryTest(entityMetaModel);
            GeneratedTestResult serviceResult = serviceTestGenerator.generateServiceTest(entityMetaModel);
            GeneratedTestResult controllerResult = controllerTestGenerator.generateControllerTest(entityMetaModel);
            GeneratedTestResult combinedResult = GeneratedTestResult.builder()
                    .entityName(entityMetaModel.getName())
                    .repositoryTestContent(repositoryResult.getRepositoryTestContent())
                    .serviceTestContent(serviceResult.getServiceTestContent())
                    .controllerTestContent(controllerResult.getControllerTestContent())
                    .builderContent(repositoryResult.getBuilderContent())
                    .build();

            combinedResult.getGeneratedFiles().addAll(repositoryResult.getGeneratedFiles());
            combinedResult.getGeneratedFiles().addAll(serviceResult.getGeneratedFiles());
            combinedResult.getGeneratedFiles().addAll(controllerResult.getGeneratedFiles());

            fileWriterService.writeGeneratedFiles(entityMetaModel, combinedResult);

            assertThat(repositoryResult.getRepositoryTestContent()).isNotNull();
            assertThat(serviceResult.getServiceTestContent()).isNotNull();
            assertThat(controllerResult.getControllerTestContent()).isNotNull();
            assertThat(repositoryResult.getBuilderContent()).isNotNull();

            assertThat(repositoryResult.getRepositoryTestContent()).contains("@DataJpaTest");
            assertThat(repositoryResult.getRepositoryTestContent()).contains("class " + entityMetaModel.getName() + "RepositoryTest");

            assertThat(serviceResult.getServiceTestContent()).contains("@ExtendWith(MockitoExtension.class)");
            assertThat(serviceResult.getServiceTestContent()).contains("class " + entityMetaModel.getName() + "ServiceTest");
            assertThat(serviceResult.getServiceTestContent()).contains("@Mock");

            assertThat(controllerResult.getControllerTestContent()).contains("@WebMvcTest");
            assertThat(controllerResult.getControllerTestContent()).contains("@WithMockUser");
            assertThat(controllerResult.getControllerTestContent()).contains("@AutoConfigureTestDatabase");
            assertThat(controllerResult.getControllerTestContent()).contains("class " + entityMetaModel.getName() + "ControllerTest");
            assertThat(controllerResult.getControllerTestContent()).contains("@MockBean");
            assertThat(controllerResult.getControllerTestContent()).contains("import org.springframework.security.test.context.support.WithMockUser");
            assertThat(serviceResult.getServiceTestContent()).contains("@InjectMocks");

            
        }

        if (!allEntities.isEmpty()) {
            GeneratedTestResult securityConfigResult = securityConfigGenerator.generateSecurityConfig(allEntities, "com.univade.TU");
            fileWriterService.writeGeneratedFiles(allEntities.get(0), securityConfigResult);

            assertThat(securityConfigResult.getSecurityConfigContent()).isNotNull();
            assertThat(securityConfigResult.getSecurityConfigContent()).contains("@TestConfiguration");
            assertThat(securityConfigResult.getSecurityConfigContent()).contains("TestSecurityConfig");
        }
    }
}
