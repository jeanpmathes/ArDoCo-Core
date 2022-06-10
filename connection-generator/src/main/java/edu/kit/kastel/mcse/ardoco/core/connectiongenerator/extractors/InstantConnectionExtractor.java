package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors;

import java.util.Map;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.AbstractExtractor;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;

public class InstantConnectionExtractor extends AbstractExtractor {
    @Configurable
    private double probability = 1.0;
    @Configurable
    private double probabilityWithoutType = 0.8;

    public InstantConnectionExtractor(DataRepository dataRepository) {
        super("InstantConnectionExtractor", dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var text = ConnectionGenerator.getAnnotatedText(dataRepository);
        var textState = ConnectionGenerator.getTextState(dataRepository);
        var modelStates = ConnectionGenerator.getModelStatesData(dataRepository);
        var recommendationStates = ConnectionGenerator.getRecommendationStates(dataRepository);
        var connectionStates = ConnectionGenerator.getConnectionStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(metamodel);
            var connectionState = connectionStates.getConnectionState(metamodel);

            findNamesOfModelInstancesInSupposedMappings(modelState, recommendationState, connectionState);
            createLinksForEqualOrSimilarRecommendedInstances(modelState, recommendationState, connectionState);
        }
    }

    /**
     * Searches in the recommended instances of the recommendation state for similar names to extracted instances. If
     * some are found the instance link is added to the connection state.
     */
    private void findNamesOfModelInstancesInSupposedMappings(IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState) {
        var recommendedInstances = recommendationState.getRecommendedInstances();
        for (IModelInstance instance : modelState.getInstances()) {
            var mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(instance, recommendedInstances);

            for (var recommendedInstance : mostLikelyRi) {
                var riProbability = recommendedInstance.getTypeMappings().isEmpty() ? probabilityWithoutType : probability;
                connectionState.addToLinks(recommendedInstance, instance, this, riProbability);
            }
        }
    }

    private void createLinksForEqualOrSimilarRecommendedInstances(IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState) {
        for (var recommendedInstance : recommendationState.getRecommendedInstances()) {
            var sameInstances = modelState.getInstances()
                    .select(instance -> SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(recommendedInstance, instance));
            sameInstances.forEach(instance -> connectionState.addToLinks(recommendedInstance, instance, this, probability));
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
