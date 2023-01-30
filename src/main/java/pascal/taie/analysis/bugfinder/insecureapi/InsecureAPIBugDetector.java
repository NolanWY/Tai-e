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
import java.util.regex.Pattern;

public class InsecureAPIBugDetector extends MethodAnalysis<Set<BugInstance>> {

    public static final String ID = "insecure-api";

    private static final Logger logger = LogManager.getLogger(InsecureAPIBugDetector.class);

    private final InsecureAPIBugConfig config;

    /*
        Store the map from methodRef(String) to the
        patterns(Set<Pattern>) that have compiled the
        regex from configuration file
    */
    private final MultiMap<String, Pattern> apiList;

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

        this.config = InsecureAPIBugConfig.readConfig("src/main/resources/insecureapi");
        this.apiList = Maps.newMultiMap();
        this.bugInfoList = Maps.newMap();
        this.allMethodRef = Sets.newHybridSet();

        this.config.getBugSet().forEach(insecureAPIBug -> {
            insecureAPIBug.insecureAPISet().forEach(insecureAPI -> {
                if(insecureAPI.paramRegex() != null) apiList.put(
                        insecureAPI.reference(), Pattern.compile(insecureAPI.paramRegex()));
                bugInfoList.put(insecureAPI, insecureAPIBug.bugInfo());
                allMethodRef.add(insecureAPI.reference());
            });
        });
    }

    @Override
    public Set<BugInstance> analyze(IR ir) {
        Set<BugInstance> bugInstances = Sets.newHybridSet();

        ir.invokes(false).filter(invoke -> {
            return allMethodRef.contains(invoke.getMethodRef().toString());
        }).forEach(invoke -> {
            String paraString = getParaString(invoke);
            APIBugInfo info = match(invoke.getMethodRef().toString(), paraString);

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
        Concat every argument to a String:
            if argument is const, append the constValue
            else append the name of the argument
        Every argument is separated by ","
    */
    private String getParaString(Invoke invoke){
        StringBuilder sb = new StringBuilder(invoke.getInvokeExp().getArgCount() * 16);

        invoke.getInvokeExp().getArgs().forEach(arg -> {
            sb.append(",")
              .append(arg.isConst() ? arg.getConstValue().toString() : arg.toString());
        });
        if(sb.length() > 0) sb.deleteCharAt(0);
        logger.info(sb);
        return sb.toString();
    }

    private APIBugInfo match(String methodRef, String paraString){
        String matchedRegex = null;

        for(Pattern pattern : apiList.get(methodRef)){
            if(pattern.matcher(paraString).matches()){
                matchedRegex = pattern.pattern();
                break;
            }
        }

        return bugInfoList.get(new InsecureAPI(methodRef, matchedRegex));
    }
}
