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

    private final Map<String, List<ParamCondToken>> infixMapSuffix;

    private static final Logger logger = LogManager.getLogger(Calculator.class);

    private Calculator(){
        infixMapSuffix = Maps.newMap();
    }

    public void infixToSuffix(String infix){
        List<ParamCondToken> tokens = ParamCondLexer.analyze(infix);
        List<ParamCondToken> suffix = Lists.newArrayList();
        Stack<ParamCondToken> stack = new Stack<>();
        logger.info(tokens);

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

        infixMapSuffix.put(infix, suffix);
        logger.info(infix + "|||" + suffix);
    }

    public boolean getResult(List<Var> vars, String infix){
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

    private boolean judgeAtom(
            ParamCondToken regex, ParamCondToken index, ParamCondToken opr, List<Var> vars){
        boolean matched = true;
        int i = index.token().charAt(1) - '0';
        if(index.token().length() == 3) i = i * 10 + (index.token().charAt(2) - '0');

        switch (opr.type()) {
            case EQ -> matched = regex.token().matches(paramString(vars.get(i)));
            case NEQ -> matched = !regex.token().matches(paramString(vars.get(i)));
        }

        return matched;
    }

    private String paramString(Var v){
        return v.isConst() ? v.getConstValue().toString() : v.toString();
    }

    public static Calculator makeInstance(){
        return new Calculator();
    }

    public static void main(String[] args){
        Calculator ca = Calculator.makeInstance();
//        ca.infixToSuffix("(p1!=/\"MD5\"/ & p2=/12<3|&^()ss/) | (p2=/null/) | (p1=/_x\\/xx/ & p3=/x x_x/)");
        ca.infixToSuffix("(p1!=/null/)");

    }
}
