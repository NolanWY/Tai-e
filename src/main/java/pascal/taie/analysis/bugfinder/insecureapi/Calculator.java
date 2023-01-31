package pascal.taie.analysis.bugfinder.insecureapi;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.C;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.util.collection.Maps;
import pascal.taie.util.collection.MultiMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {

    private Map<String, List<String>> infixMapSuffix;

    private static final Logger logger = LogManager.getLogger(Calculator.class);

    private Calculator(){
        infixMapSuffix = Maps.newMap();
    }

    public void infixToSuffix(String infix){
        List<String> tokens = toTokens(infix);
        List<String> suffix = Lists.newArrayList();
        Stack<String> stack = new Stack<>();

        for(String token : tokens){
            switch (token){
                case "(":
                case "|":
                    stack.push(token);
                    break;
                case ")":
                    while(!stack.peek().equals("("))
                        suffix.add(stack.pop());
                    stack.pop();
                    break;
                case "&":
                    while(stack.peek().equals("|"))
                        suffix.add(stack.pop());
                    stack.push(token);
                    break;
                default:
                    suffix.add(token);
            }
        }
        while(!stack.empty()){
            String s = stack.pop();
            if(!s.equals("(")) suffix.add(s);
        }

        infixMapSuffix.put(infix, suffix);
        logger.info(suffix);
    }

    public boolean getResult(Invoke invoke, String infix){
        return true;
    }

    public List<String> toTokens(String exp){
        List<String> tokens = Lists.newArrayList(); // not tai-e Lists
        Matcher ma = Pattern.compile("p[1-9]!?=</.*?/>").matcher(exp);

        int lastEnd = 0;
        while(ma.find()){
            for(int i = lastEnd; i < ma.start(); i++)
                if(exp.charAt(i) != ' ') tokens.add(String.valueOf(exp.charAt(i)));
            tokens.add(ma.group());
            lastEnd = ma.end();
        }
        for(int i = lastEnd; i < exp.length(); i++)
            tokens.add(String.valueOf(exp.charAt(i)));

        return tokens;
    }

    public static Calculator makeInstance(){
        return new Calculator();
    }

    public static void main(String[] args){
        Calculator ca = new Calculator();
        ca.infixToSuffix("(p1!=</\"MD5\"/> & p2=</12<3|&^()ss/>) | (p2=</null/>) | (p1=</_x/xx/> & p3=</x x_x/>)");

    }
}
