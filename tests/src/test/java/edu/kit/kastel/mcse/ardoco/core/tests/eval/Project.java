/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;

/**
 *
 * @author Jan Keim
 *
 */
public enum Project {
    MEDIASTORE(//
            "src/test/resources/benchmark/mediastore/original_model/ms.repository", //
            "src/test/resources/benchmark/mediastore/mediastore.txt", //
            "src/test/resources/benchmark/mediastore/goldstandard.csv", //
            new EvaluationResults(.999, .620, .765), //
            new EvaluationResults(.0, .0, .164)//
    ), //
    TEAMMATES( //
            "src/test/resources/benchmark/teammates/original_model/teammates.repository", //
            "src/test/resources/benchmark/teammates/teammates.txt", //
            "src/test/resources/benchmark/teammates/goldstandard.csv", //
            new EvaluationResults(.913, .880, .896), //
            new EvaluationResults(.0, .0, .189)//
    ), //
    TEASTORE( //
            "src/test/resources/benchmark/teastore/original_model/teastore.repository", //
            "src/test/resources/benchmark/teastore/teastore.txt", //
            "src/test/resources/benchmark/teastore/goldstandard.csv", //
            new EvaluationResults(.999, .713, .832), //
            new EvaluationResults(.0, .0, .146)),
    BIGBLUEBUTTON( //
            "src/test/resources/benchmark/bigbluebutton/original_model/bbb.repository", //
            "src/test/resources/benchmark/bigbluebutton/bigbluebutton.txt", //
            "src/test/resources/benchmark/bigbluebutton/goldstandard.csv", //
            new EvaluationResults(.877, .826, .850), //
            new EvaluationResults(.0, .0, .125));

    private static final Logger logger = LoggerFactory.getLogger(Project.class);
    private final String model;
    private final String textFile;
    private final String goldStandard;
    private final EvaluationResults expectedTraceLinkResults;
    private final EvaluationResults expectedInconsistencyResults;
    private volatile ModelConnector modelConnector = null;

    Project(String model, String textFile, String goldStandard, EvaluationResults expectedTraceLinkResults, EvaluationResults expectedInconsistencyResults) {
        this.model = model;
        this.textFile = textFile;
        this.goldStandard = goldStandard;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
        this.expectedInconsistencyResults = expectedInconsistencyResults;
    }

    public File getModelFile() {
        return new File(model);
    }

    public File getTextFile() {
        return new File(textFile);
    }

    public ModelConnector getModel() {
        if (modelConnector == null) {
            synchronized (this) {
                if (modelConnector == null) {
                    try {
                        modelConnector = new PcmXMLModelConnector(getModelFile());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return modelConnector;
    }

    public File getGoldStandardFile() {
        return new File(goldStandard);
    }

    public GoldStandard getGoldStandard(ModelConnector pcmModel) {
        return new GoldStandard(getGoldStandardFile(), pcmModel);
    }

    /**
     * @return the expectedTraceLinkResults
     */
    public EvaluationResults getExpectedTraceLinkResults() {
        return expectedTraceLinkResults;
    }

    /**
     * @return the expectedInconsistencyResults
     */
    public EvaluationResults getExpectedInconsistencyResults() {
        return expectedInconsistencyResults;
    }
}
