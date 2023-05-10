/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

public enum CodeProject {
    MEDIASTORE(//
            Project.MEDIASTORE, // Project
            "https://github.com/ArDoCo/MediaStore3.git", // Repository
            "../../temp/code/mediastore",// (temporary) code location
            "src/test/resources/gs/goldstandard-mediastore.csv", // location of the gold standard for SAM-Code
            new ExpectedResults(.0, .0, .0, .0, .0, .0) //expected results
    ),

    TEASTORE(Project.TEASTORE, // Project
            "https://github.com/ArDoCo/TeaStore.git", // Repository
            "../../temp/code/teastore",// (temporary) code location
            "src/test/resources/gs/goldstandard-teastore.csv",// location of the gold standard for SAM-Code
            new ExpectedResults(.0, .0, .0, .0, .0, .0) //expected results
    ),

    TEAMMATES(Project.TEAMMATES, // Project
            "https://github.com/ArDoCo/teammates.git",// Repository
            "../../temp/code/teammates",// (temporary) code location
            "src/test/resources/gs/goldstandard-teammates.csv",// location of the gold standard for SAM-Code
            new ExpectedResults(.0, .0, .0, .0, .0, .0) //expected results
    ),

    BIGBLUEBUTTON(Project.BIGBLUEBUTTON,// Project
            "https://github.com/ArDoCo/bigbluebutton.git",// Repository
            "../../temp/code/bigbluebutton",// (temporary) code location
            "src/test/resources/gs/goldstandard-bigbluebutton.csv",// location of the gold standard for SAM-Code
            new ExpectedResults(.0, .0, .0, .0, .0, .0) //expected results
    ),

    JABREF(Project.JABREF, // Project
            "https://github.com/ArDoCo/jabref.git",// Repository
            "../../temp/code/jabref",// (temporary) code location
            "src/test/resources/gs/goldstandard-jabref.csv", // location of the gold standard for SAM-Code
            new ExpectedResults(.0, .0, .0, .0, .0, .0) //expected results
    );

    private static final Logger logger = LoggerFactory.getLogger(Project.class);

    private final String codeRepository;
    private final String codeLocation;
    private final String samCodeGoldStandardLocation;
    private final Project project;
    private final ExpectedResults expectedResults;

    CodeProject(Project project, String codeRepository, String codeLocation, String samCodeGoldStandardLocation, ExpectedResults expectedResults) {
        this.project = project;
        this.codeRepository = codeRepository;
        this.codeLocation = codeLocation;
        this.samCodeGoldStandardLocation = samCodeGoldStandardLocation;
        this.expectedResults = expectedResults;
    }

    public Project getProject() {
        return project;
    }

    public String getCodeRepository() {
        return codeRepository;
    }

    public String getCodeLocation() {
        return codeLocation;
    }

    public ExpectedResults getExpectedResults() {
        return expectedResults;
    }

    public ImmutableList<String> getSamCodeGoldStandard() {
        File samCodeGoldStandardFile = new File(samCodeGoldStandardLocation);
        var path = Paths.get(samCodeGoldStandardFile.toURI());

        List<String> lines = Lists.mutable.empty();
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        lines.remove(0);

        MutableList<String> goldStandard = Lists.mutable.empty();
        for (var line : lines) {
            var parts = line.split(",");
            String modelElementId = parts[0];
            String codeElementId = parts[2];
            goldStandard.add(TestUtil.createTraceLinkString(modelElementId, codeElementId));
        }
        return goldStandard.toImmutable();
    }

}
