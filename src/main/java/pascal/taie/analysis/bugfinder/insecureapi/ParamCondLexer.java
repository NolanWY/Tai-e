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

import java.util.List;
import java.util.regex.Pattern;

class ParamCondLexer {

    private static final Rule[] rules = {
            new Rule(Pattern.compile(" +"), ParamCondTokenType.NO_TYPE),
            new Rule(Pattern.compile("p[1-9][0-9]*"), ParamCondTokenType.INDEX),
            new Rule(Pattern.compile("/"), ParamCondTokenType.REGEX),
            new Rule(Pattern.compile("="), ParamCondTokenType.EQ),
            new Rule(Pattern.compile("!="), ParamCondTokenType.NEQ),
            new Rule(Pattern.compile("&"), ParamCondTokenType.AND),
            new Rule(Pattern.compile("\\|"), ParamCondTokenType.OR),
            new Rule(Pattern.compile("\\("), ParamCondTokenType.LEFT_PARENTHESES),
            new Rule(Pattern.compile("\\("), ParamCondTokenType.RIGHT_PARENTHESES),
    };

    public static List<ParamCondToken> analyze(String expr) {
        // TODO
        return null;
    }

    private record Rule(Pattern pattern, ParamCondTokenType type) {
    }
}
