/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProvider;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.architecture.inconsistencies.baseline.InconsistencyBaseline;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExplicitEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.HoldElementsBackModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ResultCalculator;

class InconsistencyIT {
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyIT.class);

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String ADDITIONAL_CONFIG = null;

    private File inputText;
    private File inputModel;
    private PcmXMLModelConnector pcmModel;

    private File additionalConfigs = null;
    private final File outputDir = new File(OUTPUT);

    @AfterEach
    void afterEach() {
        if (ADDITIONAL_CONFIG != null) {
            var config = new File(ADDITIONAL_CONFIG);
            config.delete();
        }
        if (additionalConfigs != null) {
            additionalConfigs = null;
        }
    }

    /**
     * Tests the inconsistency detection on all {@link Project projects}.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @DisplayName("Evaluate Inconsistency Analyses")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(Project.class)
    void inconsistencyIT(Project project) {
        Map<ModelInstance, DataStructure> runs = produceHoldBackRunResults(project, false);

        ResultCalculator resultCalculator = calculateEvaluationResults(project, runs);
        var weightedResults = resultCalculator.getWeightedAveragePRF1();

        EvaluationResults expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResults(project, weightedResults, expectedInconsistencyResults);
        checkResults(weightedResults, expectedInconsistencyResults);
    }

    private ResultCalculator calculateEvaluationResults(Project project, Map<ModelInstance, DataStructure> runs) {
        ResultCalculator resultCalculator = new ResultCalculator();
        for (var run : runs.entrySet()) {
            var runEvalResults = evaluateRun(project, run.getKey(), run.getValue());
            if (runEvalResults != null) {
                int fn = runEvalResults.getFalseNegative().size();
                int fp = runEvalResults.getFalsePositives().size();
                int tp = runEvalResults.getTruePositives().size();
                resultCalculator.nextEvaluation(tp, fp, fn);
            }
        }
        return resultCalculator;
    }

    /**
     * Tests the baseline approach that reports an inconsistency for each sentence that is not traced to a model
     * element. This test is enabled by providing the environment variable "testBaseline" with any value.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluate Inconsistency Analyses Baseline")
    @ParameterizedTest(name = "Evaluating Baseline For {0}")
    @EnumSource(Project.class)
    void inconsistencyBaselineIT(Project project) {
        Map<ModelInstance, DataStructure> runs = produceHoldBackRunResults(project, true);

        ResultCalculator resultCalculator = calculateEvaluationResults(project, runs);
        var weightedResults = resultCalculator.getWeightedAveragePRF1();

        EvaluationResults expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResults(project, weightedResults, expectedInconsistencyResults);
    }

    private Map<ModelInstance, DataStructure> produceHoldBackRunResults(Project project, boolean useBaselineApproach) {
        Map<ModelInstance, DataStructure> runs = new HashMap<>();

        var name = project.name().toLowerCase();
        inputModel = project.getModelFile();
        inputText = project.getTextFile();

        var holdElementsBackModelConnector = constructHoldElementsBackModelConnector();

        ArDoCo arDoCoBaseRun;
        try {
            arDoCoBaseRun = definePipelineBase(inputText, holdElementsBackModelConnector, additionalConfigs, useBaselineApproach);
        } catch (IOException e) {
            Assertions.fail(e);
            return runs;
        }
        arDoCoBaseRun.run();
        var baseRunData = new DataStructure(arDoCoBaseRun.getDataRepository());
        runs.put(null, baseRunData);

        for (int i = 0; i < holdElementsBackModelConnector.numberOfInstances(); i++) {
            holdElementsBackModelConnector.setCurrentHoldBackId(i);
            var currentHoldBack = holdElementsBackModelConnector.getCurrentHoldBack();
            var currentRun = defineArDoCoWithPreComputedData(baseRunData, holdElementsBackModelConnector, additionalConfigs, useBaselineApproach);
            currentRun.run();
            runs.put(currentHoldBack, new DataStructure(currentRun.getDataRepository()));
        }
        return runs;
    }

    private HoldElementsBackModelConnector constructHoldElementsBackModelConnector() {
        try {
            this.pcmModel = new PcmXMLModelConnector(this.inputModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new HoldElementsBackModelConnector(pcmModel);
    }

    private static ArDoCo definePipelineBase(File inputText, HoldElementsBackModelConnector holdElementsBackModelConnector, File additionalConfigsFile,
            boolean useInconsistencyBaseline) throws FileNotFoundException {
        ArDoCo arDoCo = new ArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        var additionalConfigs = ArDoCo.loadAdditionalConfigs(additionalConfigsFile);

        arDoCo.addPipelineStep(ArDoCo.getTextProvider(inputText, additionalConfigs, dataRepository));

        addMiddleSteps(holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(ArDoCo.getInconsistencyChecker(additionalConfigs, dataRepository));
        }

        return arDoCo;
    }

    private static ArDoCo defineArDoCoWithPreComputedData(DataStructure precomputedData, HoldElementsBackModelConnector holdElementsBackModelConnector,
            File additionalConfigsFile, boolean useInconsistencyBaseline) {
        ArDoCo arDoCo = new ArDoCo();
        var dataRepository = arDoCo.getDataRepository();
        var additionalConfigs = ArDoCo.loadAdditionalConfigs(additionalConfigsFile);

        var preprocessingData = new PreprocessingData(precomputedData.getText());
        dataRepository.addData(PreprocessingData.ID, preprocessingData);

        addMiddleSteps(holdElementsBackModelConnector, arDoCo, dataRepository, additionalConfigs);

        if (useInconsistencyBaseline) {
            arDoCo.addPipelineStep(new InconsistencyBaseline(dataRepository));
        } else {
            arDoCo.addPipelineStep(ArDoCo.getInconsistencyChecker(additionalConfigs, dataRepository));
        }
        return arDoCo;
    }

    private static void addMiddleSteps(HoldElementsBackModelConnector holdElementsBackModelConnector, ArDoCo arDoCo, DataRepository dataRepository,
            Map<String, String> additionalConfigs) {
        arDoCo.addPipelineStep(new ModelProvider(dataRepository, holdElementsBackModelConnector));
        arDoCo.addPipelineStep(ArDoCo.getTextExtraction(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ArDoCo.getRecommendationGenerator(additionalConfigs, dataRepository));
        arDoCo.addPipelineStep(ArDoCo.getConnectionGenerator(additionalConfigs, dataRepository));
    }

    private ExplicitEvaluationResults evaluateRun(Project project, ModelInstance removedElement, DataStructure data) {
        var modelId = data.getModelIds().get(0);

        ImmutableList<MissingModelInstanceInconsistency> inconsistencies = getInconsistencies(data, modelId);
        if (removedElement == null) {
            // base case
            // TODO
            return null;
        }

        var goldStandard = project.getGoldStandard(this.pcmModel);
        var expectedLines = goldStandard.getSentencesWithElement(removedElement).distinct().collect(i -> i.toString()).castToCollection();
        var actualSentences = inconsistencies.collect(MissingModelInstanceInconsistency::sentence).distinct().collect(i -> i.toString()).castToCollection();

        return TestUtil.compare(actualSentences, expectedLines);
    }

    private ImmutableList<MissingModelInstanceInconsistency> getInconsistencies(DataStructure data, String modelId) {
        ImmutableList<Inconsistency> inconsistencies = data.getInconsistencyState(modelId).getInconsistencies();
        return inconsistencies.select(i -> MissingModelInstanceInconsistency.class.isAssignableFrom(i.getClass()))
                .collect(MissingModelInstanceInconsistency.class::cast);
    }

    private void logResults(Project project, EvaluationResult results, EvaluationResults expectedResults) {
        if (logger.isInfoEnabled()) {
            String infoString = String.format(Locale.ENGLISH,
                    "\n%s:\n\tPrecision:\t%.3f (min. expected: %.3f)%n\tRecall:\t\t%.3f (min. expected: %.3f)%n\tF1:\t\t%.3f (min. expected: %.3f)",
                    project.name(), results.getPrecision(), expectedResults.getPrecision(), results.getRecall(), expectedResults.getRecall(), results.getF1(),
                    expectedResults.getF1());
            logger.info(infoString);
        }
    }

    private void checkResults(EvaluationResult results, EvaluationResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(),
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(),
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(),
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.getF1()));
    }

}
