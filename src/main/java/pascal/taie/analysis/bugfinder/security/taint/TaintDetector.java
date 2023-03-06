package pascal.taie.analysis.bugfinder.security.taint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.bugfinder.BugInstance;
import pascal.taie.analysis.pta.PointerAnalysis;
import pascal.taie.analysis.pta.PointerAnalysisResult;
import pascal.taie.analysis.pta.plugin.taint.TaintAnalysis;
import pascal.taie.analysis.pta.plugin.taint.TaintFlow;
import pascal.taie.config.AnalysisConfig;

import java.util.Set;

public abstract class TaintDetector extends ProgramAnalysis<Set<BugInstance>> {

    private static final Logger logger = LogManager.getLogger(TaintDetector.class);

    protected static final Set<TaintFlow> taintFlows;

    static {
        PointerAnalysisResult ptaResult = World.get().getResult(PointerAnalysis.ID);
        if(ptaResult != null && ptaResult.hasResult(TaintAnalysis.class.getName())) {
            taintFlows = ptaResult.getResult(TaintAnalysis.class.getName());
        } else {
            taintFlows = Set.of();
            logger.warn("Taint analysis should be run before TaintDetector starts");
        }
    }

    public TaintDetector(AnalysisConfig config) {
        super(config);
    }
}
