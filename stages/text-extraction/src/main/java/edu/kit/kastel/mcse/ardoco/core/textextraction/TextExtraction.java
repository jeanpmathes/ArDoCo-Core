/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.textextraction.agents.InitialTextAgent;
import edu.kit.kastel.mcse.ardoco.core.textextraction.agents.PhraseAgent;

/**
 * The Class TextExtractor.
 */
public class TextExtraction extends AbstractExecutionStage {

    /**
     * Instantiates a new text extractor.
     */
    public TextExtraction(DataRepository dataRepository) {
        super(List.of(new InitialTextAgent(dataRepository), new PhraseAgent(dataRepository)), TextExtraction.class.getSimpleName(), dataRepository);

    }

    /**
     * Creates a {@link TextExtraction} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of InconsistencyChecker
     */
    public static TextExtraction get(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var textExtractor = new TextExtraction(dataRepository);
        textExtractor.applyConfiguration(additionalConfigs);
        return textExtractor;
    }

    @Override
    protected void initializeState() {
        var dataRepository = getDataRepository();
        var optionalTextState = dataRepository.getData(TextState.ID, TextStateImpl.class);
        if (optionalTextState.isEmpty()) {
            var textState = new TextStateImpl();
            dataRepository.addData(TextState.ID, textState);
        }
    }
}
