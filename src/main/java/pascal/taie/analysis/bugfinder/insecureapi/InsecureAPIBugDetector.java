/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package pascal.taie.analysis.bugfinder.insecureapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.analysis.MethodAnalysis;
import pascal.taie.analysis.bugfinder.BugInstance;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.util.collection.Maps;
import pascal.taie.util.collection.MultiMap;
import pascal.taie.util.collection.Sets;

import java.util.Map;
import java.util.Set;

public class InsecureAPIBugDetector extends MethodAnalysis<Set<BugInstance>> {

    public static final String ID = "insecure-api";

    private static final Logger logger = LogManager.getLogger(InsecureAPIBugDetector.class);

    /*
        Store the map from methodRef(String) to the
        patterns(Set<Pattern>) that have compiled the
        regex from configuration file
    */
    private final MultiMap<String, String> apiList;

    /*
        Store the map from InsecureAPI to APIBugInfo
        it's convenient to get the information to create
        the BugInstance via this map
    */
    private final Map<InsecureAPI, APIBugInfo> bugInfoList;

    // Store all methodRef in the configuration file
    private final Set<String> allMethodRef;

    public InsecureAPIBugDetector(AnalysisConfig config){
        super(config);

        InsecureAPIBugConfig config1 =
                InsecureAPIBugConfig.readConfig("src/main/resources/insecureapi");
        this.apiList = Maps.newMultiMap();
        this.bugInfoList = Maps.newMap();
        this.allMethodRef = Sets.newHybridSet();

        config1.getBugSet().forEach(insecureAPIBug
                -> insecureAPIBug.insecureAPISet().forEach(insecureAPI
                -> {
            if(insecureAPI.paramRegex() != null) {
                apiList.put(
                        insecureAPI.reference(), insecureAPI.paramRegex());
                Calculator.infixToSuffix(insecureAPI.paramRegex());
            }
            bugInfoList.put(insecureAPI, insecureAPIBug.bugInfo());
            allMethodRef.add(insecureAPI.reference());
        }));
    }

    @Override
    public Set<BugInstance> analyze(IR ir) {
        Set<BugInstance> bugInstances = Sets.newHybridSet();

        ir.invokes(false).filter(invoke
                -> allMethodRef.contains(invoke.getMethodRef().toString())).forEach(invoke
                -> {
            APIBugInfo info = match(invoke);

            if(info != null){
                BugInstance bugInstance = new BugInstance(
                        info.bugType(), info.severity(), ir.getMethod())
                        .setSourceLine(invoke.getLineNumber());
                bugInstances.add(bugInstance);
            }
        });

        return bugInstances;
    }

    /*
        Use the information of invoke to get APIBugInfo
        APIBugInfo may be null, which means matching failed
     */
    private APIBugInfo match(Invoke invoke){
        String matchedRegex = null;

        for(String exp : apiList.get(invoke.getMethodRef().toString())){
            if(Calculator.getResult(invoke.getInvokeExp().getArgs(), exp)){
                matchedRegex = exp;
                break;
            }
        }

        return bugInfoList.get(new InsecureAPI(invoke.getMethodRef().toString(), matchedRegex));
    }
}
