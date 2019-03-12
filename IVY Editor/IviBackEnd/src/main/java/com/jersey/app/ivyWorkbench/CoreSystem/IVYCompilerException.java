
package com.jersey.app.ivyWorkbench.CoreSystem;

public class IVYCompilerException extends Exception {

    private static final int MAX_MSG_LENGTH = 150;
    private int lineNumber;
    private String expr;
    private String error;
    private String globalExpr;
    private int endLineNumber;

    public IVYCompilerException(int lineNumber, String expr, String error) {
        this.lineNumber = lineNumber;
        this.expr = expr;
        this.error = error;
        this.globalExpr = expr;
        this.endLineNumber = lineNumber;
    }

   public IVYCompilerException(String string) {
        this.lineNumber = 0;
        this.expr = "";
        this.error = string;
    }

    /**
     * The error message
     *
     * @return a string with the error message
     */
    @Override
    public String getMessage () {
        StringBuilder sb = new StringBuilder();
        sb.append(lineNumber);
        sb.append(": Error at \"");
        sb.append(expr);
        sb.append("\"");
        sb.append(" (");
        sb.append(error);
        sb.append(")");
        if (globalExpr!=null && !expr.equals(globalExpr)) {
            sb.append("\n\nOffending expression was found in definition from lines ");
            sb.append(lineNumber);
            sb.append(" to ");
            sb.append(endLineNumber);
            sb.append(":\n");
            if (globalExpr.length() > MAX_MSG_LENGTH) {
                sb.append(globalExpr.substring(0, MAX_MSG_LENGTH));
                sb.append("...");
            }
            else
                sb.append(globalExpr);
        }
        return sb.toString();
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setGlobalExpr(String expr) {
        this.globalExpr = expr;
    }

    public void setEndLineNumber(int i) {
        this.endLineNumber = i;
    }

}