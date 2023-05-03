package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;

public class Required extends DependentHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return calculateReq(archComponent, compUnit);
    }

    private Confidence calculateReq(ArchitectureComponent archEndpoint, CodeCompilationUnit compUnit) {
        if (!getNodeResult().getConfidence(new EndpointTuple(archEndpoint, compUnit)).hasValue()) {
            return new Confidence();
        }
        Set<ArchitectureComponent> allLinks = new HashSet<>();
        Set<Entity> linkedEndpoints = getNodeResult().getLinkedEndpoints(compUnit);
        for (Entity linkedEndpoint : linkedEndpoints) {
            if (linkedEndpoint instanceof ArchitectureComponent comp) {
                allLinks.add(comp);
            }
        }
        if (allLinks.size() <= 1) {
            return new Confidence();
        }
        if (hasRequired(archEndpoint, allLinks)) { // TODO also subcomponents
            return new Confidence(1.0);
        }
        return new Confidence();
    }

    // requiresComp requires (at least one of) providesComp
    private static boolean hasRequired(ArchitectureComponent requiresComp, Set<ArchitectureComponent> providesComps) {
        for (ArchitectureComponent providesComp : providesComps) {
            if (isRequired(requiresComp, providesComp)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRequired(ArchitectureComponent requires, ArchitectureComponent provides) {
        for (ArchitectureInterface req : requires.getRequiredInterfaces()) {
            if (provides.getProvidedInterfaces().contains(req)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Required";
    }
}
