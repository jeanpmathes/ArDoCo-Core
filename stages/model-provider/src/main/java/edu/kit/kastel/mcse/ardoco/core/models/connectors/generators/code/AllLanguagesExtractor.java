/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java.JavaExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.shell.ShellExtractor;

public final class AllLanguagesExtractor extends CodeExtractor {

    private final Map<ProgrammingLanguage, CodeExtractor> codeExtractors;

    public AllLanguagesExtractor(String path) {
        super(path);
        codeExtractors = Map.of(ProgrammingLanguage.JAVA, new JavaExtractor(path), ProgrammingLanguage.SHELL, new ShellExtractor(path));
    }

    @Override
    public CodeModel extractModel() {
        List<CodeModel> models = new ArrayList<>();
        for (CodeExtractor extractor : codeExtractors.values()) {
            var model = extractor.extractModel();
            models.add(model);
        }
        Set<CodeItem> codeEndpoints = new HashSet<>();
        for (CodeModel model : models) {
            codeEndpoints.addAll(model.getContent());
        }
        CodeModel finalModel = new CodeModel(codeEndpoints);
        return finalModel;
    }
}
