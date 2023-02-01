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

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.ir.exp.Var;
import pascal.taie.util.collection.Maps;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Calculator {

    /*
        Map from infix to suffix
        Infix is a logical expression String
        Suffix is a list of ParamCondToken which can be calculated to a boolean
        Cache this map can avoid multiple calculations
    */
    private static final Map<String, List<ParamCondToken>> infixMapSuffix
            = Maps.newMap();

    private static final Logger logger = LogManager.getLogger(Calculator.class);

    /*
        Transfer infix to suffix and store the map
     */
    public static void infixToSuffix(String infix){
        List<ParamCondToken> tokens = ParamCondLexer.analyze(infix);
        List<ParamCondToken> suffix = Lists.newArrayList();
        Stack<ParamCondToken> stack = new Stack<>();

        for(ParamCondToken token : tokens){
            switch (token.type()){
                case EQ, NEQ, AND, LEFT_PARENTHESES:
                    stack.add(token);
                    break;
                case RIGHT_PARENTHESES:
                    while(!stack.peek().type().equals(ParamCondTokenType.LEFT_PARENTHESES))
                        suffix.add(stack.pop());
                    stack.pop();
                    break;
                case OR:
                    while(!stack.empty() && stack.peek().type().equals(ParamCondTokenType.AND))
                        suffix.add(stack.pop());
                    stack.add(token);
                    break;
                case INDEX:
                    suffix.add(token);
                    break;
                case REGEX:
                    suffix.add(token);
                    suffix.add(stack.pop());
                    break;
                case NO_TYPE: break;
            }
        }
        while(!stack.empty()) suffix.add(stack.pop());

        infixMapSuffix.put(infix, suffix);
    }

    /*
        Calculate logical expression suffix expression to boolean
     */
    public static boolean getResult(List<Var> vars, String infix){
        List<ParamCondToken> suffix = infixMapSuffix.get(infix);
        Stack<ParamCondToken> paramStack = new Stack<>();
        Stack<Boolean> boolStack = new Stack<>();

        for(ParamCondToken token : suffix){
            switch (token.type()) {
                case INDEX, REGEX -> paramStack.add(token);
                case NEQ, EQ ->
                        boolStack.add(judgeAtom(paramStack.pop(), paramStack.pop(), token, vars));
                case AND -> boolStack.add(boolStack.pop() && boolStack.pop());
                case OR -> boolStack.add(boolStack.pop() || boolStack.pop());
            }
        }

        return boolStack.pop();
    }

    /*
        Calculate the atom logical expression "pn=xxx"
     */
    private static boolean judgeAtom(
            ParamCondToken regex, ParamCondToken index, ParamCondToken opr, List<Var> vars){
        boolean matched = true;
        int i = Integer.parseInt(index.token());

        switch (opr.type()) {
            case EQ -> matched = paramString(vars.get(i - 1)).matches(regex.token());
            case NEQ -> matched = !paramString(vars.get(i - 1)).matches(regex.token());
        }

        return matched;
    }

    /*
        Get the String of Var
            if Var is const, return the constValue
            else return the name of Var
     */
    private static String paramString(Var v){
        return v.isConst() ? v.getConstValue().toString() : v.toString();
    }
}
