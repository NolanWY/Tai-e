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

import pascal.taie.util.AnalysisException;
import pascal.taie.util.collection.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParamCondLexer {

    private static final List<Rule> rules = List.of(
            Rule.getRule(" +", ParamCondTokenType.NO_TYPE),
            Rule.getRule("p[1-9][0-9]*", ParamCondTokenType.INDEX),
            Rule.getRule("/((?:[^/\\\\]|\\\\.)*)/", ParamCondTokenType.REGEX),
            Rule.getRule("=", ParamCondTokenType.EQ),
            Rule.getRule("!=", ParamCondTokenType.NEQ),
            Rule.getRule("&", ParamCondTokenType.AND),
            Rule.getRule("\\|", ParamCondTokenType.OR),
            Rule.getRule("\\(", ParamCondTokenType.LEFT_PARENTHESES),
            Rule.getRule("\\)", ParamCondTokenType.RIGHT_PARENTHESES)
            );

    private ParamCondLexer() {
    }

    public static List<ParamCondToken> analyze(String expr) {
        List<ParamCondToken> tokenList = new ArrayList<>();
        List<Matcher> matchers = Lists.map(rules, rule -> rule.pattern.matcher(expr));
        int position = 0;
        outer:
        while(position < expr.length()) {
            for(int i = 0; i < rules.size(); i++) {
                Matcher matcher = matchers.get(i);
                if(matcher.region(position, expr.length()).lookingAt()) {
                    ParamCondTokenType type = rules.get(i).type;
                    switch(type) {
                        case NO_TYPE -> {}
                        case REGEX -> tokenList.add(new ParamCondToken(type,
                                matcher.group(1).replace("\\/", "/")));
                        default -> tokenList.add(new ParamCondToken(type, matcher.group()));
                    }
                    position = matcher.end();
                    continue outer;
                }
            }
            throw new AnalysisException("Failed to match tokens at position " + position + " in " + expr);
        }
        return tokenList;
    }

    private record Rule(Pattern pattern, ParamCondTokenType type) {

        public static Rule getRule(String regex, ParamCondTokenType type) {
            return new Rule(Pattern.compile(regex), type);
        }
    }
}
