
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions;

import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jfc
 */
public class IVYMatcher implements MatchResult {
    
    public static final Pattern ParenExpression =
            Pattern.compile("\\A\\s*\\((.*)\\)\\s*\\z");
    public static final Pattern CurlyBracesExpression =
            Pattern.compile("\\A\\s*\\{(.*)\\}\\s*\\z");
    // TODO: validar .*\\W / \\W.*
//    public static final Pattern UExpression =
//            Pattern.compile("\\A\\s*\\[(.*\\W)U(\\W.*)\\]\\s*\\z");

    private static String unescaped(String operator) {
        return operator.startsWith("\\")? operator.substring(1) : operator;
    }

    private String operator;
    private String expr;
    private ArrayList<String> list = null;

    public IVYMatcher(String op, String expr) {
        this.operator = op;
        this.expr = expr;
    }

    public boolean matches() {
        if (operator.equals("()"))
            return matchParenthesis();
        if (operator.equals("{}"))
            return matchSetEnumeration();
        if (operator.equals("AG") || operator.equals("EG")
         || operator.equals("AF") || operator.equals("EF")
         || operator.equals("AX") || operator.equals("EX")
         || operator.equals("A[") || operator.equals("E["))
            return matchCTLop();
        else
            return matchOthers();
    }

    private boolean matchCTLop() {
        expr = expr.trim();
        if (!expr.startsWith(operator))
            return false;
        this.list = new ArrayList<String>();
        this.list.add(expr);
// TODO: Verify if A/E are used. Does not look like from above...
        if (operator.equals("A") || operator.equals("E"))
            this.list.add(expr.substring(1));
        else
            this.list.add(expr.substring(2));
        return true;

    }

    private boolean matchParenthesis() {
        Matcher m = ParenExpression.matcher(expr);

        if (m.matches() && this.balanced(m.group(1))) {
            this.list = new ArrayList<String>();
            this.list.add(expr);
            this.list.add(m.group(1));
            return true;
        } else {
            this.list = null;
            return false;
        }
    }

    private boolean matchSetEnumeration() {
        Matcher m = CurlyBracesExpression.matcher(expr);

        if (m.matches()) {
            this.list = new ArrayList<String>();
            this.list.add(expr);
            this.list.add(m.group(1));
            return true;
        } else {
            this.list = null;
            return false;
        }
    }

/*    private boolean matchSquareParenthesis() {
        Matcher m = UExpression.matcher(lexpr);

        if (m.matches()) {
            this.list = new ArrayList<String>();
            this.list.add(lexpr);
            this.list.add(m.group(1));
            this.list.add(m.group(2));
            return true;
        } else {
            this.list = null;
            return false;
        }
    }*/

    private boolean matchOthers() {
        String lexpr = this.expr;
        if (operator.equals("U")) {
            lexpr = lexpr.trim();
            // Note: [ is removed by the E[/A[ matchers...
            if (!lexpr.endsWith("]"))
                return false;
            lexpr = lexpr.substring(0, lexpr.length()-1);
        }
        String[] arr = lexpr.split(operator);
        String subexpr = "";

        if (arr.length==1) return false;

        this.list = new ArrayList<String>();
        this.list.add(lexpr);

        for(String s: arr) {
            if (!subexpr.equals(""))
                s = subexpr + IVYMatcher.unescaped(operator) + s;
            if (this.balanced(s)) {
                this.list.add(s);
                subexpr = "";
            } else
                subexpr = s;
        }
        if (this.list.size()==2 || !this.balanced(subexpr)) {
            this.list = null;
            return false;
        }

        return true;
    }

    public int start() {
        if (this.list == null) throw new IllegalStateException();
        return 0;
    }

    public int start(int group) {
        if (this.list == null) throw new IllegalStateException();
        int aggregPointer = 0;

        for(int i=1; i<group; i++)
            aggregPointer += (list.get(i).length() + 1);

        return aggregPointer;
    }

    public int end() {
        if (this.list == null) throw new IllegalStateException();
        
        return this.list.get(0).length();
    }

    public int end(int group) {
        if (this.list == null) throw new IllegalStateException();
        int aggregPointer = 0;

        for(int i=1; i<=group; i++)
            aggregPointer += (list.get(i).length() + 1);

        return aggregPointer-1;
    }

    public String group() {
        if (this.list == null) throw new IllegalStateException();

        return this.list.get(1);
    }

    public String group(int group) {
        if (this.list == null) throw new IllegalStateException();

        return this.list.get(group);
    }

    public void groupDelete(int group) {
        if (this.list == null) throw new IllegalStateException();

        this.list.remove(group);
    }

    public int groupCount() {
        if (this.list == null) throw new IllegalStateException();

        return this.list.size()-1;
    }

    private boolean balanced(String s) {
        int balance = 0;
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i)==')')
                if (balance>0) balance--;
                else
                    return false;
            if (s.charAt(i)=='(') balance++;
        }
        return balance==0;
    }

    @Override
    public String toString() {
        if (this.list == null) throw new IllegalStateException();
        StringBuilder s = new StringBuilder();

        for(int i=1; i<list.size(); i++)
            // TODO: Why &???
            s.append(list.get(i)).append("&");
        s.deleteCharAt(s.length());
        return s.toString();
    }



}
