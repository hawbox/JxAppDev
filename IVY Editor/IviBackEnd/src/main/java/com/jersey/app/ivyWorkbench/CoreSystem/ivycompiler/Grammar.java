
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;
import java.util.regex.Pattern;

/**
 *
 * @author jfc
 */
public class Grammar {
    public static final Pattern KeyWordPattern =
            Pattern.compile("\\s*(?:[dD][eE][fF][iI][nN][eE][sS]|"+
                            "[tT][yY][pP][eE][sS]|"+
                            "[iI][nN][tT][eE][rR][aA][cC][tT][oO][rR]\\s*(\\S.*)|"+
                            "[aA][tT][tT][rR][iI][bB][uU][tT][eE][sS]|"+
                            "[aA][cC][tT][iI][oO][nN][sS]|"+
                            "[aA][xX][iI][oO][mM][sS]|"+
                            "[aA][gG][gG][rR][eE][gG][aA][tT][eE][sS]|"+
                            "[iI][nN][cC][lL][uU][dD][eE][sS]|"+
                            "[tT][eE][sS][tT]|"+
                            "[tT][eE][sS][tT]\\s*-\\s*\\S.*|"+
                            "[fF][aA][iI][rR][nN][eE][sS][sS])");

    public static final Pattern DefinesPattern = Pattern.compile("\\s*(\\w+)\\s*=\\s*(\\S.*)");
    public static final short DefName = 1;
    public static final short DefExpression = 2;
    
    public static final String CommentChar = "#";

    public static final Pattern EnumTypePattern =
            Pattern.compile("\\s*(\\w+)\\s*=\\s*\\{\\s*(\\w+\\s*(?:\\,\\s*\\w+\\s*)*)\\}");

    public static final Pattern RangeTypePattern =
            Pattern.compile("\\s*(\\w+)\\s*=\\s*(\\w+)\\s*\\.\\.\\s*(\\w+)");
    public static final short RangeLowerBound = 2;
    public static final short RangeUpperBound = 3;

    public static final Pattern ArrayTypePattern =
            Pattern.compile("\\s*(\\w+)\\s*=\\s*array\\s*(\\w+)\\s*\\.\\.\\s*(\\w+)\\s*of\\s*(\\S.*)");
    public static final short ArrayType = 4;

    public static final Pattern BooleanTypePattern =
            Pattern.compile("\\s*(\\w+)\\s*=\\s*[bB][oO][oO][lL][eE][aA][nN]");

    private static final String PlainInteractorDef = "[iI][nN][tT][eE][rR][aA][cC][tT][oO][rR]\\s*(\\w+)";
    private static final String ParamsDef = "\\(\\s*(\\w+(?:\\s*,\\s*\\w+)*)\\s*\\)";
    private static final String SimpleActionDef = "\\w+(?:\\(\\s*\\w+(?:\\s*,\\s*\\w+)*\\s*\\))?";

    public static final Pattern PlainInteractorPattern =
            Pattern.compile(PlainInteractorDef);

    public static final Pattern ParamInteractorPattern =
            Pattern.compile(PlainInteractorDef+ParamsDef);

    public static final Pattern AttributesPattern =
            Pattern.compile("\\s*(?:\\[vis\\])?\\s*(\\w+(?:\\s*,\\s*\\w+)*)\\s*\\:\\s*(\\S.*)");

    public static final Pattern ActionsPattern =
            Pattern.compile("\\s*(?:\\[vis\\])?\\s*("+SimpleActionDef+")((?:\\s+"+SimpleActionDef+")*)");
            
    public static final Pattern ActionPattern =
            Pattern.compile("\\s*(\\w+)(?:"+ParamsDef+")?");
//            Pattern.compile("\\s*(?:\\[vis\\])?\\s*(\\w+)(?:"+ParamsDef+")?");
//          Pattern.compile("\\s*(?:\\[vis\\])?\\s*(\\w+(?:\\.\\w+)*)(?:\\((\\S.*)\\))?");

    public static final Pattern AggregationPattern =
            Pattern.compile("\\s*(\\w+)(?:"+ParamsDef+")?\\s*via\\s*(\\w+)");
//            Pattern.compile("\\s*(\\w+)(?:\\(\\w+\\))?\\s*via\\s*(\\w+)");

    public static final Pattern InitAxiomPattern =
            Pattern.compile("\\s*\\[\\]\\s*(\\S.*)");

    public static final Pattern PermissionAxiomPattern =
            Pattern.compile("\\s*per\\(([^\\-]*)\\)\\s*\\->\\s*(\\S.*)");
//            Pattern.compile("\\s*per\\((.*)\\)\\s*\\->\\s*(\\S.*)");

    public static final Pattern ObligationAxiomPattern =
            Pattern.compile("\\s*(\\S.*)\\s*\\->\\s*obl\\((.*)\\)");

    // TODO: action expression ([^\\]]*) needs to be made more specific
    // currently is does not accept arrays
    public static final Pattern ModalAxiomPattern =
            Pattern.compile("\\s*(?:(\\S.*)\\s*\\->\\s*)?\\[([^\\]]*)\\]\\s*(\\S.*)");


    public static final Pattern NotExpression =
            Pattern.compile("\\A\\s*\\!(.*)\\s*\\z");
    public static final Pattern MinusExpression =
            Pattern.compile("\\A\\s*\\-(.*)\\s*\\z");
    public static final Pattern AttribExpression =
            Pattern.compile("\\A\\s*((?:\\w+\\.)*)(\\w+)(?:\\[(\\w+(?:\\]\\[\\w+)*)\\])?(\\')?\\s*\\z");
    public static final Pattern KeepExpression =
            Pattern.compile("\\A\\s*keep\\(\\s*(\\w+(?:\\.\\w+)*(?:\\s*,\\s*\\w+(?:\\.\\w+)*)*\\s*)\\)\\s*\\z");
    public static final Pattern InSetExpression =
            Pattern.compile("\\A\\s*((?:\\w+\\.)*\\w+(?:\\')?)\\s+in\\s+\\{\\s*(\\w+(?:\\s*,\\s*\\w+)*)\\s*\\}\\s*\\z");
    public static final Pattern InSetWithDefRefExpression =
            Pattern.compile("\\A\\s*((?:\\w+\\.)*\\w+(?:\\')?)\\s+in\\s+(\\w+)\\s*\\z");

    private static final String ActionRefDef = "((?:\\w+\\.)*)(\\w+)(?:\\(((?:\\w+\\.)*\\w+(?:\\s*,\\s*(?:\\w+\\.)*\\w+)*)?\\))?";
    public static final Pattern SingleActionRefExpression =
                   Pattern.compile("\\A\\s*((?:\\w+\\.)*)(\\w+)(?:\\(((?:\\w+\\.)*\\w+(?:\\s*,\\s*(?:\\w+\\.)*\\w+)*)?\\))?\\s*\\z");
    public static final Pattern ActionRefExpression =
                   Pattern.compile("\\A\\s*"+ActionRefDef+"(?:\\s*\\|\\s*"+ActionRefDef+")+\\s*\\z");

    public static final Pattern EffectExpression =
            Pattern.compile("\\A\\s*effect\\("+ActionRefDef+"\\)\\s*\\z");
    public static final Pattern EffectedExpression =
            Pattern.compile("\\A\\s*effected\\("+ActionRefDef+"\\)\\s*\\z");

    public static final Pattern VoidExpression =
            Pattern.compile("^(\\s|\\t)*$");
    
    /**
     * Checks if an expression is a keyword.
     * 
     * @param expr the string with the expression
     * @return true if the string holds a keyword, false otherwise.
     */
    public static boolean isKeyWord(String expr) {
        return Grammar.KeyWordPattern.matcher(expr).matches();
    }

    /**
     * Checks if a line is a comment line.
     * 
     * @param expr the string with the expression
     * @return true if the string holds a comment, false otherwise.
     */
    public static boolean isInlineComment(String expr) {
        return expr.startsWith(CommentChar);
    }

}
