/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler;

import com.jersey.app.ivyWorkbench.CoreSystem.ICompiler;
import com.jersey.app.ivyWorkbench.CoreSystem.IVYCompilerException;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.axioms.*;
import com.jersey.app.ivyWorkbench.CoreSystem.ivycompiler.expressions.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to compile an IVY model.
 *
 * @author Jose.Campos@di.uminho.pt, edited by Paolo Masci
 */
public class Compiler implements ICompiler {

    public static final short defines = 1;

    private String lastReadLine = "";
    private int indent = 0, lineNumber = 0, readLineNumber = 0;
    public IVYModel model = new IVYModel();
    public IVYInteractor currentInteractor = null;

    /**
     * @param args name of input file
     */
    public static void main(String[] args) throws IVYCompilerException, IOException, Exception {
        String ifilename, smvfilename;

 //       if (args.length==0)
        //           throw new IVYCompilerException("You need to provide a file to be complied as an input parameter!");
        ifilename = "~/Work/Models/fromMDH/bbraunv3.i"; //args[0]; //args.length < 1? getInputFileName() : args[0];
        smvfilename = args.length < 2 ? null : args[0];

        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(ifilename));
        } catch (FileNotFoundException e) {
            System.exit(1);
        }

        Compiler c = new Compiler();
        String smvCode = c.compileModel(scanner).toSMV();

        FileWriter fout = new FileWriter(getOutputFileName(smvfilename));
        fout.write(smvCode);
        fout.close();
    }

    /**
     * Creates a compiler.
     *
     */
    public Compiler() {
        lastReadLine = "";
        indent = 0;
        lineNumber = 0;
        readLineNumber = 0;
        model = new IVYModel();
        model.addType(new IVYBooleanType(this));
        currentInteractor = null;
    }

    /**
     * Compiles a IVY model.
     *
     * @param scanner a scanner of the text of the model
     * @return the compiled model (this)
     */
    @Override
    public ICompiler compileModel(Scanner scanner) throws IVYCompilerException, Exception {
        model = parseModel(scanner);
        model.unfoldInteractors();
        model.unfoldAxioms();
        model.unfoldActions();
        model.unfoldArrays();
        model.setupStuttering(this);
        // model.expandTypes();
        //return model.toSMV();
        return this;
    }

    public IVYModel getModel() {
        return model;
    }

    /**
     * Determine the common variable names for a list of IVYActionRefExpression
     *
     * @param list the list of actions
     * @return the list of names
     */
    // TO DO: should be in IVYActionRefExpression class
    private static List<String> getCommonParamNames(ArrayList<IVYActionRefExpression> list) {
        List<String> res = new ArrayList<String>();

        res.addAll(list.get(0).getParamNames());

        for (int i = 1; i < list.size(); i++) {
            res.retainAll(list.get(i).getParamNames());
        }

        return res;
    }

    /**
     * Parses a reference to an action. Validates parameters' types if they
     * exist.
     *
     * @param expr the expression to parse
     * @return a list of IVYActionRefExpression objects for the parsed
     * expression.
     * @throws ivycompiler.IVYCompilerException
     */
    private ArrayList<IVYActionRefExpression> parseActionUse(String expr) throws IVYCompilerException {
        ArrayList<IVYActionRefExpression> res = new ArrayList<IVYActionRefExpression>();
        String def = model.getDefinition(expr);
        if (def != null) // Check if we have a definition name and replace it
        {
            expr = def;   // CHECK: Use IVYDefRefExpression?
        }
        Matcher m = Pattern.compile("!\\((.*)\\)").matcher(expr);
        boolean b = m.matches();
        String[] actions = (b ? m.group(1) : expr).split("\\|");

        for (String action : actions) {
            m = Grammar.SingleActionRefExpression.matcher(action);
            if (!m.matches()) {
                throw new IVYCompilerException(lineNumber, expr, "not a valid action definition: " + action);
            }
            res.add(this.parseSingleActionRefExpression(m, new ArrayList<String>()));
            if (b && res.size() > 1
                    && (res.get(0).getPath() == null ? res.get(res.size() - 1).getPath() != null : Arrays.equals(res.get(0).getPath(), res.get(res.size() - 1).getPath()))) {
                throw new IVYCompilerException(lineNumber, expr, "all actions must be from the same interactor");
            }
        }
        if (b) {
            ArrayList<IVYActionRefExpression> negated = new ArrayList<IVYActionRefExpression>();
            Set<String> nomes = new HashSet<String>();
            for (IVYActionRefExpression ref : res) {
                nomes.add(ref.getName());
            }
            for (IVYAction ac : currentInteractor.getActions()) {
                if (!nomes.contains(ac.getName())) {
                    List<IVYExpression> params = new ArrayList<IVYExpression>();
                    for (IVYType t : ac.getTypes()) {
                        params.add(new IVYAttribExpression(null, "_", false));
                    }
                    negated.add(new IVYActionRefExpression(res.get(0).getPath(), ac.getName(), params, false, false));
                }
            }
            res = negated;
        }
        return res;
    }

    /**
     * Parse a CTL expression
     *
     * @param expr the CTL expression
     * @return the parsed expression
     */
    private IVYExpression parseCTL(String expr) throws IVYCompilerException {
        return this.parseExpression(expr, new ArrayList<String>(), true);
    }

    /**
     * Parse the text into a IVY model object.
     *
     * @param scanner a scanner over the text of the model
     * @return an IVYModel object
     * @throws IVYCompilerException
     * @throws Exception
     */
    private IVYModel parseModel(Scanner scanner) throws IVYCompilerException, Exception {
        String expr = getExpression(scanner);

        try {
            while (!expr.equals("")) {
                if (expr.equalsIgnoreCase("defines") && currentInteractor == null) {
                    expr = getExpression(scanner);
                    while (!expr.isEmpty() && !Grammar.isKeyWord(expr.trim())) {
                        model.addDefine(parseDefines(expr));
                        expr = getExpression(scanner);
                    }
                } else if (expr.equalsIgnoreCase("types") && currentInteractor == null) {
                    expr = getExpression(scanner);
                    while (!expr.isEmpty() && !Grammar.isKeyWord(expr.trim())) {
                        model.addType(parseType(expr));
                        expr = getExpression(scanner);
                    }
                } else if (expr.toLowerCase().contains("interactor")) {
                    if (currentInteractor != null) {
                        model.addInteractor(currentInteractor);
                    }
                    currentInteractor = null;
                    Matcher plainMatcher = Grammar.PlainInteractorPattern.matcher(expr);
                    Matcher paramMatcher = Grammar.ParamInteractorPattern.matcher(expr);
                    if (plainMatcher.matches()) {
                        currentInteractor = new IVYInteractor(plainMatcher.group(Grammar.DefName), this.model);
                    } else if (paramMatcher.matches()) {
                        String[] ltypesnames = paramMatcher.group(2).split(",");
                        List<IVYEmptyType> ltypes = new ArrayList<IVYEmptyType>();
                        for (int i = 0; i < ltypesnames.length; i++) {
                            ltypes.add(new IVYEmptyType(ltypesnames[i].trim()));
                        }
                        currentInteractor = new IVYInteractor(paramMatcher.group(Grammar.DefName), ltypes, this.model);

                    } else {
                        throw new IVYCompilerException(lineNumber, expr, "Illegal interactor declaration");
                    }
                    expr = getExpression(scanner);
                } else if (expr.equalsIgnoreCase("attributes") && currentInteractor != null) {
                    expr = getExpression(scanner);
                    while (!expr.isEmpty() && !Grammar.isKeyWord(expr.trim())) {
                        for (IVYAttribute a : parseAttribute(expr)) {
                            currentInteractor.addAttribute(a);
                        }
                        expr = getExpression(scanner);
                    }
                } else if (expr.equalsIgnoreCase("actions") && currentInteractor != null) {
                    expr = getExpression(scanner);
                    try {
                        while (!expr.isEmpty() && !Grammar.isKeyWord(expr.trim())) {
                            for (IVYAction a : parseAction(expr)) {
                                currentInteractor.addAction(a);
                            }
                            expr = getExpression(scanner);
                        }
                        if (currentInteractor.getActions().size() > 0) {
                            currentInteractor.addAction(new IVYAction("nil", new ArrayList(), this));
                        }
                    } catch (IVYCompilerException e) { // Acrescentar a linha ao erro...
                        e.setLineNumber(lineNumber);
                        e.setGlobalExpr(expr);
                        e.setEndLineNumber(readLineNumber - 1);
                        throw e;
                    }
                } else if (expr.equalsIgnoreCase("axioms") && currentInteractor != null) {
                    expr = getExpression(scanner);
                    try {
                        while (!expr.isEmpty() && !Grammar.isKeyWord(expr.trim())) {
                            currentInteractor.addAxiom(parseAxiom(expr));
                            expr = getExpression(scanner);
                        }
                    } catch (IVYCompilerException e) { // Acrescentar a linha ao erro...
                        e.setLineNumber(lineNumber);
                        e.setGlobalExpr(expr);
                        e.setEndLineNumber(readLineNumber - 1);
                        throw e;
                    }
                } else if ((expr.equalsIgnoreCase("aggregates") | expr.equalsIgnoreCase("includes")) && currentInteractor != null) {
                    expr = getExpression(scanner);
                    while (!expr.isEmpty() && !Grammar.isKeyWord(expr.trim())) {
                        currentInteractor.addAggregation(parseAggregation(expr));
                        expr = getExpression(scanner);
                    }
                } else if ((expr.equalsIgnoreCase("fairness")) && currentInteractor != null) {
                    expr = getExpression(scanner);
                    currentInteractor.addFairness(parseFairness(expr));
                    expr = getExpression(scanner);
                    System.out.println("Fairness: " + expr);
                } else if (expr.toLowerCase().startsWith("test") && currentInteractor != null) {
                    String[] aux = expr.split("-", 2);
                    String descr = aux.length > 1 ? aux[1] : null;
                    expr = getExpression(scanner);
                    while (!expr.isEmpty() && !Grammar.isKeyWord(expr.trim())) {
                        IVYExpression t = this.parseCTL(expr);
                        t.setComment(descr);
                        currentInteractor.addCTLAxiom(t);
                        expr = getExpression(scanner);

//  FALTA IR AOS BINARY AND UNARY EXPRESSION FAZER O TRATAMENTO DO CTL!!!!!
                    }
                } else {
                    throw new IVYCompilerException(lineNumber, expr, "unknown or misplaced keyword");
                }
            }
            if (currentInteractor != null) {
                model.addInteractor(currentInteractor);
            }
        } catch (Exception e) {
            if (e instanceof IVYCompilerException) {
                throw e;
            } else {
                throw new IVYCompilerException("Unknown error at line: " + lineNumber + ": " + expr + " (" + e.toString() + ")");
            }
        }
        return model;
    }
    
    /**
     * Parses an expression
     *
     * @param expr
     * @return
     * @throws IVYCompilerException
     */
    private IVYDefine parseDefines(String expr) throws IVYCompilerException {
        Matcher m = Grammar.DefinesPattern.matcher(expr);

        if (!m.matches()) {
            throw new IVYCompilerException(lineNumber, expr, "not a defines");
        }

        IVYDefine idef = new IVYDefine(m.group(Grammar.DefName), m.group(Grammar.DefExpression));

        return idef;
    }

// TODO: rework this to make it indenpenden from type declaration
//  split into parseTypeDef and parseType
// TODO: accept  type = type  (e.g. typename = boolean)
    /**
     * Parse an type to IVY type.
     *
     * @param expr
     * @return
     * @throws IVYCompilerException
     */
    private IVYType parseType(String expr) throws IVYCompilerException {
        Matcher mEnum = Grammar.EnumTypePattern.matcher(expr);
        Matcher mRange = Grammar.RangeTypePattern.matcher(expr);
        Matcher mArray = Grammar.ArrayTypePattern.matcher(expr);
        Matcher mBoolean = Grammar.BooleanTypePattern.matcher(expr);
        IVYType idef = null;

        if (mEnum.matches()) {
            String tname = mEnum.group(Grammar.DefName);
            List<IVYConstExpression> tvalues = new ArrayList<IVYConstExpression>();
            IVYEnumerationExpression defExpr;
            for (String v : mEnum.group(2).split(",")) {
                String val = v.trim();
                String def = model.getDefinition(val);
                if (def != null) { // TODO: Use IVYDefRefExpression
                    defExpr = this.parseEnumerationExpression(def);
                    tvalues.addAll(defExpr.getValues());
                } else {
                    tvalues.add(new IVYConstExpression(val));
                }
            }
            idef = new IVYEnumType(tname, tvalues, this);
        } else if (mRange.matches()) {
            String tname = mRange.group(Grammar.DefName);
            String lowerBound = validateBound(mRange.group(Grammar.RangeLowerBound));
            String upperBound = validateBound(mRange.group(Grammar.RangeUpperBound));
            idef = new IVYRangeType(tname, lowerBound, upperBound, this);
        } else if (mArray.matches()) {
            String tname = mArray.group(Grammar.DefName);
            String lowerBound = validateBound(mArray.group(Grammar.RangeLowerBound));
            String upperBound = validateBound(mArray.group(Grammar.RangeUpperBound));
            IVYType tdef = parseType(mArray.group(Grammar.ArrayType));
            idef = new IVYArrayType(tname, lowerBound, upperBound, tdef, this);
        } else if (mBoolean.matches()) {
            String tname = mBoolean.group(Grammar.DefName);
            idef = new IVYBooleanType(this);
        } else if (expr.equalsIgnoreCase("boolean")) {
            idef = new IVYBooleanType(this);
        } else if (expr.matches("\\w+") && (this.model.getType(expr) != null)) {
            idef = this.model.getType(expr);
        } else {
            throw new IVYCompilerException(lineNumber, expr, "not a type");
        }

        return idef;
    }

    /**
     * Parse an type, to IVY type
     *
     * @param expr
     * @return
     * @throws IVYCompilerException
     */
    private List<IVYAttribute> parseAttribute(String expr) throws IVYCompilerException {
        Matcher m = Grammar.AttributesPattern.matcher(expr);

        if (!m.matches()) {
            throw new IVYCompilerException(lineNumber, expr, "not a valid attribute(s) definition");
        }

        String[] names = m.group(Grammar.DefName).split(",");
        String type = m.group(Grammar.DefExpression);

        IVYType t;
        if (currentInteractor.hasParam(type)) {
            t = new IVYEmptyType(type);
        } else if (model.hasType(type)) {
            t = model.getType(type);
        } else {
            t = parseType("type_" + type.replaceAll("\\W", "_") + " = " + type);
            model.addType(t);
        }

        List<IVYAttribute> res = new ArrayList<IVYAttribute>();
        for (String name : names) {
            if (currentInteractor.usedId(name) || this.model.hasConstant(name)) {
                throw new IVYCompilerException(lineNumber, name, "duplicated identifer and/or constant name");
            }
            res.add(new IVYAttribute(name.trim(), t, this));
        }

        return res;
    }

    /**
     * Parses an action declaration.
     *
     * @param expr the text to parse
     * @return an IVYAction object
     * @throws ivycompiler.IVYCompilerException
     */
    private List<IVYAction> parseAction(String expr) throws IVYCompilerException {
        List<IVYAction> res = new ArrayList<IVYAction>();
        Matcher ms = Grammar.ActionsPattern.matcher(expr);
        String subexpr;

        if (!ms.matches()) {
            throw new IVYCompilerException(lineNumber, expr, "not a valid action(s) definition");
        }

        do {
            subexpr = ms.group(1);
            Matcher m = Grammar.ActionPattern.matcher(subexpr);

            if (!m.matches()) // playing it safe - should never happen!
            {
                throw new IVYCompilerException(lineNumber, expr, "not a valid action definition");
            }

            String name = m.group(Grammar.DefName);
            String type = m.groupCount() > 1 ? m.group(Grammar.DefExpression) : null;
            String[] typesNames;
            List<IVYType> types = new ArrayList<IVYType>();

            if (type != null) {
                String typeName;
                typesNames = type.split(",");

                for (String s : typesNames) {
                    typeName = s.trim();
                    if (currentInteractor.hasParam(typeName)) {
                        types.add(new IVYEmptyType(typeName));
                    } else if (model.hasType(typeName)) {
                        types.add(model.getType(typeName));
                    } else {
                        throw new IVYCompilerException(0, expr, "Unknown type: " + s + " in action declaration.");
                    }
                }
            }
            res.add(new IVYAction(name, types, this));

            subexpr = ms.group(2);
            ms = Grammar.ActionsPattern.matcher(subexpr);

        } while (ms.matches());
        return res;
    }

    /**
     * Parse an axiom
     *
     * @param expr the text to be parsed
     * @return the parsed axiom as an IVYObject
     * @throws ivycompiler.IVYCompilerException
     */
    private IVYAxiom parseAxiom(String expr) throws IVYCompilerException {
        Matcher initAxMatcher = Grammar.InitAxiomPattern.matcher(expr);
        Matcher perAxMatcher = Grammar.PermissionAxiomPattern.matcher(expr);
        Matcher oblAxMatcher = Grammar.ObligationAxiomPattern.matcher(expr);
        Matcher modalAxMatcher = Grammar.ModalAxiomPattern.matcher(expr);
        IVYExpression iexpr;
        ArrayList<IVYActionRefExpression> acRefsList;
        IVYAxiom iax;
        List<String> paramNames;

        if (initAxMatcher.matches()) {
            iexpr = this.parseExpression(initAxMatcher.group(1), new ArrayList<String>(), false);
            iax = new IVYInitAxiom(iexpr);
        } else if (perAxMatcher.matches()) {
            acRefsList = parseActionUse(perAxMatcher.group(1).trim());
            paramNames = getCommonParamNames(acRefsList);
            iexpr = this.parseExpression(perAxMatcher.group(2).trim(), paramNames, false); // throws exception below if it does not work!!!
            iax = new IVYPermissionAxiom(acRefsList, iexpr);
        } else if (oblAxMatcher.matches()) {
            acRefsList = parseActionUse(oblAxMatcher.group(2).trim());
            paramNames = getCommonParamNames(acRefsList);
            iexpr = this.parseExpression(oblAxMatcher.group(1).trim(), paramNames, false);
            iax = new IVYObligationAxiom(acRefsList, iexpr);
        } else if (modalAxMatcher.matches()) {
            String pre = modalAxMatcher.group(1);
            IVYExpression iexpr2 = null;
            if (pre != null) {
                iexpr2 = this.parseExpression(pre.trim(), new ArrayList<String>(), false);
            }
            acRefsList = parseActionUse(modalAxMatcher.group(2).trim());
            paramNames = getCommonParamNames(acRefsList);
            iexpr = this.parseExpression(modalAxMatcher.group(3).trim(), paramNames, false);
            iax = new IVYModalAxiom(iexpr2, acRefsList, iexpr);
        } else {
            iexpr = this.parseExpression(expr.trim(), new ArrayList<String>(), false);
            iax = new IVYInvariantAxiom(iexpr);
        } /* else (currentInteractor.usedId(name))
         throw new IVYCompilerException(lineNumber, expr, "unrecognised or badly formed axiom");*/

        return iax;
    }

    /**
     * Parse the fairness expression
     *
     * @param expr the text to be parsed
     * @return the parsed axiom as an IVYObject
     * @throws ivycompiler.IVYCompilerException
     */
    private IVYAxiom parseFairness(String expr) throws IVYCompilerException {
        IVYExpression iexpr;
        IVYAxiom iax;
        List<String> paramNames;

        iexpr = this.parseExpression(expr.trim(), new ArrayList<String>(), true);
        iax = new IVYFairnessAxiom(iexpr);

        return iax;
    }

    /**
     * Parses an Aggregation to IVY aggregation TODO: Currently a Type parameter
     * cannot be used in teh aggregation...
     *
     * @param expr
     * @return
     * @throws IVYCompilerException
     */
    private IVYAggregation parseAggregation(String expr) throws IVYCompilerException {
        Matcher m = Grammar.AggregationPattern.matcher(expr);

        if (!m.matches()) {
            throw new IVYCompilerException(lineNumber, expr, "not a valid aggregation definition");
        }

        String name = m.groupCount() > 2 ? m.group(3) : m.group(2);
        String iname = m.group(1);
        String tparam = m.groupCount() > 2 ? m.group(2) : null;
        String[] typesNames = new String[0];
        String typeName;
        List<IVYType> types = new ArrayList<IVYType>();

        if (currentInteractor.usedId(name)) {
            throw new IVYCompilerException(lineNumber, expr, "duplicated identifier: " + name);
        }
        if (tparam != null) {
            typesNames = tparam.split(",");

            for (String s : typesNames) {
                typeName = s.trim();
                if (!model.hasType(typeName)) {
                    throw new IVYCompilerException(lineNumber, expr, "Not a know type: " + typeName);
                }
                types.add(model.getType(typeName));
            }
        }

        return new IVYAggregation(name, iname, types);
    }

    /**
     * Validate a bound
     *
     * @param bound
     * @return
     * @throws IVYCompilerException
     */
    private String validateBound(String bound) throws IVYCompilerException {
        String def = this.model.getDefinition(bound);
        if (def != null) {
            bound = def;
        }

        try {
            Integer.parseInt(bound);
        } catch (NumberFormatException e) {
            throw new IVYCompilerException(lineNumber, bound, "not a number");
        }

        return bound;
    }

    /**
     * Get an expression for a given scanner
     *
     * @param scanner
     * @return
     */
    private String getExpression(Scanner scanner) {
        String currentLine;
        StringBuilder expr = new StringBuilder(" ");
        currentLine = lastReadLine.equals("") ? getNextLine(scanner) : lastReadLine;
        lineNumber = readLineNumber;
        indent = getIndent(currentLine);
        if (!Grammar.isKeyWord(currentLine.trim())) {
            do {
                expr.append(currentLine.trim());
                expr.append(" ");
//                expr.append(System.getProperty("line.separator"));
                currentLine = getNextLine(scanner);
            } while (indent < getIndent(currentLine));
            lastReadLine = currentLine;
        } else {
            expr.append(currentLine);
            lastReadLine = "";
        }

        return expr.toString().trim();
    }

    /**
     * Return an line indent
     *
     * @param line
     * @return
     */
    private static int getIndent(String line) {
        int spcCount = 0;

        while (spcCount < line.length() && line.charAt(spcCount) == ' ') {
            spcCount++;
        }

        return spcCount;
    }

    /**
     * Unused method
     *
     * @return
     */
    private static String getInputFileName() {
        String filename = "";
        Scanner sc = new Scanner(System.in);

        while (filename.length() == 0) {
            System.out.print("Name a file to be complied: (" + System.getProperty("user.dir") + "): ");
            try {
                filename = sc.nextLine();
            } catch (Exception e) {
                System.err.println("Error reading file name!");
                System.exit(1);
            }
        }
        return filename;
    }

    /**
     * Return the output file name
     *
     * @param inputname
     * @return
     */
    private static String getOutputFileName(String inputname) {
        Scanner sc = new Scanner(inputname);

        sc.useDelimiter("\\.");
        return sc.next() + ".smv";
    }

    /**
     * Return the next line for a given canner
     *
     * @param scanner
     * @return
     */
    private String getNextLine(Scanner scanner) {
        String readLine = "";
        String[] arr;
        boolean comment;
        if (scanner.hasNextLine()) {
            do {
                arr = scanner.nextLine().split("#", 2);
                readLineNumber++;
                readLine = arr.length > 0 ? arr[0] : "";
                comment = false;
                //comment=readLine.startsWith("/*");
                //comment= (comment && !readLine.endsWith("*/"));
            } while ((comment || isEmpty(readLine)) && scanner.hasNextLine());
            if (comment || isEmpty(readLine)) {
                readLine = "";
            }
        }
        return readLine;
    }

    /**
     * Is a line empty?
     *
     * @param s
     * @return
     */
    private static boolean isEmpty(String s) {
        String line = new String(s); //is this correct??

        line = line.trim();
        return line.startsWith("/*") || line.endsWith("*/") || line.isEmpty();
    }

    /**
     * Parses an expression with propositional and (if wanted) CTL operators
     *
     * @param expr the expression to be parsed
     * @param extraVars variables allowed in the scope of the expression
     * @param ctl whether CTL operators are allowed
     * @return the parsed expression
     * @throws ivycompiler.IVYCompilerException
     */
    public IVYExpression parseExpression(String expr, List<String> extraVars, boolean ctl) throws IVYCompilerException {

        IVYMatcher ivyMatcher = new IVYMatcher("()", expr);
        if (ivyMatcher.matches()) {
            return new IVYUnaryExpression("()", parseExpression(ivyMatcher.group(1), extraVars, ctl));
        }

        ivyMatcher = new IVYMatcher("AG", expr);
        if (ivyMatcher.matches()) {
            if (ctl) {
                return new IVYUnaryExpression("AG", parseExpression(ivyMatcher.group(1), extraVars, ctl));
            }
            throw new IVYCompilerException(0, expr, "AG CTL expression not allowed in the current context.");
        }

        ivyMatcher = new IVYMatcher("EG", expr);
        if (ivyMatcher.matches()) {
            if (ctl) {
                return new IVYUnaryExpression("EG", parseExpression(ivyMatcher.group(1), extraVars, ctl));
            }
            throw new IVYCompilerException(0, expr, "EG CTL expression not allowed in the current context.");
        }

        ivyMatcher = new IVYMatcher("AF", expr);
        if (ivyMatcher.matches()) {
            if (ctl) {
                return new IVYUnaryExpression("AF", parseExpression(ivyMatcher.group(1), extraVars, ctl));
            }
            throw new IVYCompilerException(0, expr, "AF CTL expression not allowed in the current context.");
        }

        ivyMatcher = new IVYMatcher("EF", expr);
        if (ivyMatcher.matches()) {
            if (ctl) {
                return new IVYUnaryExpression("EF", parseExpression(ivyMatcher.group(1), extraVars, ctl));
            }
            throw new IVYCompilerException(0, expr, "EF CTL expression not allowed in the current context.");
        }

        ivyMatcher = new IVYMatcher("AX", expr);
        if (ivyMatcher.matches()) {
            if (ctl) {
                return new IVYUnaryExpression("AX", parseExpression(ivyMatcher.group(1), extraVars, ctl));
            }
            throw new IVYCompilerException(0, expr, "AX CTL expression not allowed in the current context.");
        }

        ivyMatcher = new IVYMatcher("EX", expr);
        if (ivyMatcher.matches()) {
            if (ctl) {
                return new IVYUnaryExpression("EX", parseExpression(ivyMatcher.group(1), extraVars, ctl));
            }
            throw new IVYCompilerException(0, expr, "EX CTL expression not allowed in the current context.");
        }

        // TODO: check U operator -needs to be treated properly in IVYMatcher!!!
        ivyMatcher = new IVYMatcher("A[", expr);
        if (ivyMatcher.matches()) {
            if (ctl) {
                return parseUExpression(ivyMatcher.group(1), extraVars, "A[");
            }
            throw new IVYCompilerException(0, expr, "A[U] CTL expression not allowed in the current context.");
        }

        ivyMatcher = new IVYMatcher("E[", expr);
        if (ivyMatcher.matches()) {
            if (ctl) {
                return parseUExpression(ivyMatcher.group(1), extraVars, "E[");
            }
            throw new IVYCompilerException(0, expr, "E[U] CTL expression not allowed in the current context.");
        }

        ivyMatcher = new IVYMatcher("&", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("&", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("\\|", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("|", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("<->", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("<->", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("->", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("->", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("!=", expr);
        if (ivyMatcher.matches()) {
            return new IVYBinaryExpression("!=", parseExpression(ivyMatcher.group(1), extraVars, ctl),
                    parseExpression(ivyMatcher.group(2), extraVars, ctl));
        }

        ivyMatcher = new IVYMatcher(">=", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression(">=", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher(">", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression(">", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("<=", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("<=", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("<", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("<", ivyMatcher, extraVars, ctl);
        }

//        Matcher eqMatcher = EqExpression.ivyMatcher(expr);
        ivyMatcher = new IVYMatcher("=", expr);
        if (ivyMatcher.matches()) {
            return new IVYEqualityExpression(
                    parseExpression(ivyMatcher.group(1), extraVars, ctl),
                    parseExpression(ivyMatcher.group(2), extraVars, ctl));
        }

        ivyMatcher = new IVYMatcher("\\+", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("+", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("-", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("-", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("\\*", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("*", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("/", expr);
        if (ivyMatcher.matches()) {
            return parseBinaryExpression("/", ivyMatcher, extraVars, ctl);
        }

        ivyMatcher = new IVYMatcher("{}", expr);
        if (ivyMatcher.matches()) {
            return new IVYUnaryExpression("{}", parseEnumerationExpression(ivyMatcher.group(1)));
        }

        Matcher stdMatcher = Grammar.NotExpression.matcher(expr);
        if (stdMatcher.matches()) {
            IVYExpression subexpr = parseExpression(stdMatcher.group(1), extraVars, ctl);
            if (subexpr instanceof IVYActionRefExpression) {
                ((IVYActionRefExpression) subexpr).setNegated(true);
                return subexpr;
            }
            return new IVYUnaryExpression("!", subexpr);
        }

        stdMatcher = Grammar.MinusExpression.matcher(expr);
        if (stdMatcher.matches()) {
            return new IVYUnaryExpression("-", parseExpression(stdMatcher.group(1), extraVars, ctl));
        }

        stdMatcher = Grammar.AttribExpression.matcher(expr);
        if (stdMatcher.matches()) {
            return parseAtribbConstActionDefExpression(stdMatcher, extraVars, ctl);
        }

        stdMatcher = Grammar.KeepExpression.matcher(expr);
        if (stdMatcher.matches()) {
            return parseKeepExpression(stdMatcher.group(1), extraVars);
        }

        stdMatcher = Grammar.EffectExpression.matcher(expr);
        if (stdMatcher.matches()) {
            if (ctl) {
                throw new IVYCompilerException(0, expr, "effect operator not allowed in CTL expressions. You probably meant to use effected?");
            }

            // TODO priming could be removed if allways assumed do to effect operator (aka only)
            IVYActionRefExpression refExpr = parseSingleActionRefExpression(stdMatcher, extraVars);
            refExpr.setPrime(true);
            return refExpr;
        }

        stdMatcher = Grammar.EffectedExpression.matcher(expr);
        if (stdMatcher.matches()) {
            if (!ctl) {
                throw new IVYCompilerException(0, expr, "effected operator not allowed outside CTL expressions. You probably meant to use effect?");
            }
            IVYActionRefExpression refExpr = parseSingleActionRefExpression(stdMatcher, extraVars);
            return refExpr;
        }

        stdMatcher = Grammar.InSetExpression.matcher(expr);
        if (stdMatcher.matches()) {
            return parseInSetExpression(stdMatcher.group(1), stdMatcher.group(2), extraVars);
        }

        stdMatcher = Grammar.InSetWithDefRefExpression.matcher(expr);
        if (stdMatcher.matches()) {
            return parseInSetWithDefRefExpression(stdMatcher.group(1), stdMatcher.group(2), extraVars);
        }
// TODO: variables, constants, numbers and multi-level references missing!!!
        stdMatcher = Grammar.VoidExpression.matcher(expr);
        if (stdMatcher.matches()) {
            throw new IVYCompilerException(0, expr, "Empty expression. Maybe you have two consecutive operators?");
        }
        throw new IVYCompilerException(0, expr, "Unrecognized or unparsable expression.");

    }

    /**
     * Parses a reference to an action. Validates parameters' types if they
     * exist.
     *
     * @param m the ivyMatcher with the expression
     * @param extraVars list of extra (local) variables to be considered in the
     * context of the expression.
     * @return an IVYActionRefExpression object with the parsed expression.
     * @throws CoreSystem.Server.IVYCompilerException
     */
    public IVYActionRefExpression parseSingleActionRefExpression(Matcher m, List<String> extraVars) throws IVYCompilerException {
        String sPath = m.group(1);
        String[] path = sPath.equals("") ? null : sPath.split("\\.");
        String acName = m.group(2);
        String sParams = m.group(3);
        String[] acParams = sParams == null ? null : sParams.split(",");
        List<IVYExpression> paramsList = new ArrayList<IVYExpression>();

        IVYInteractor inter = this.model.getInteractor(this.currentInteractor, path);
        IVYAction acDef = inter.getAction(acName);
        if (acDef == null && !acName.equals("nil")) {
            throw new IVYCompilerException(0, acName, "Undefined action.");
        }
        if (acParams != null) {
            List<IVYType> types = acDef.getTypes();
            if (types.size() != acParams.length) {
                throw new IVYCompilerException(0, m.group(0).trim(), "Wrong number of arguments in action.");
            }
            for (int i = 0; i < acParams.length; i++) {
                String param = acParams[i].trim();
                Matcher pMatcher = Grammar.AttribExpression.matcher(param);
                if (!pMatcher.matches()) {
                    throw new IVYCompilerException(0, m.group(0).trim(), "Invalid parameter - " + param + " -  in action.");
                }
                IVYExpression paramExpr = parseAtribbConstActionDefExpression(pMatcher, extraVars, false);
                IVYType type = types.get(i);
                if (type != null && !type.contains(paramExpr)) //if type null, must be paramater
                {
                    throw new IVYCompilerException(0, m.group(0).trim(), param + " is not in type " + types.get(i));
                }
                paramsList.add(paramExpr);
            //    acParams[i] = acParams[i].trim();
                //    IVYAttribute inter = ModelCompiler.currentInteractor.getAttribute(acParams[i]);
                //    if (inter==null)
                //        inter = ModelCompiler.model.getInteractor(ModelCompiler.currentInteractor,path).getAttribute(acParams[i]);
                //    String def = ModelCompiler.model.getDefinition(acParams[i]);
                //    if (!acParams[i].startsWith("_") && inter==null && !acParams[i].matches("[+-]?\\d*") && !ModelCompiler.model.hasConstant(acParams[i]) && def==null)
            }
        }
        // TODO: Remove priming!! (no longer needed due to effect operator)
        return new IVYActionRefExpression(path, acName, paramsList, false, false);
    }

    /**
     * unused method - TODO: ROMOVE?!
     *
     * @param m
     * @param extraVars
     * @return
     * @throws IVYCompilerException
     */
    /*private IVYActionRefExpression parseOredActionRefExpression(Matcher m, ArrayList<String> extraVars) throws IVYCompilerException {
     String sPath = m.group(1);
     String[] path = sPath.equals("")? null: sPath.split("\\.");
     String acName = m.group(2);
     String sParams = m.group(3);
     String[] acParams = sParams==null ? null : sParams.split(",");
     List<IVYExpression> paramsList = new ArrayList<IVYExpression>();

     if (acParams!=null) {
     IVYInteractor inter = this.model.getInteractor(this.currentInteractor,path);
     IVYAction acDef = inter.getAction(acName);
     if (acDef == null)
     throw new IVYCompilerException(0,acName,"Undefined action.");
     List<String> types = acDef.getTypes();
     if (types.size() != acParams.length)
     throw new IVYCompilerException(0, m.group(0).trim(), "Wrong number of arguments in action.");
     for (int i = 0; i<acParams.length; i++) {
     String param = acParams[i].trim();
     Matcher pMatcher = Grammar.AttribExpression.matcher(param);
     if (!pMatcher.matches())
     throw new IVYCompilerException(0, m.group(0).trim(), "Invalid parameter - "+param+" -  in action.");
     IVYExpression paramExpr = parseAtribbConstActionDefExpression(pMatcher, extraVars, false);
     IVYType type = this.model.getType(types.get(i));
     if (!type.contains(paramExpr))
     throw new IVYCompilerException(0, m.group(0).trim(), param+" is not in type "+types.get(i));
     paramsList.add(paramExpr);
     //    acParams[i] = acParams[i].trim();
     //    IVYAttribute inter = ModelCompiler.currentInteractor.getAttribute(acParams[i]);
     //    if (inter==null)
     //        inter = ModelCompiler.model.getInteractor(ModelCompiler.currentInteractor,path).getAttribute(acParams[i]);
     //    String def = ModelCompiler.model.getDefinition(acParams[i]);
     //    if (!acParams[i].startsWith("_") && inter==null && !acParams[i].matches("[+-]?\\d*") && !ModelCompiler.model.hasConstant(acParams[i]) && def==null)
     }
     }
     // TODO: Remove priming!! (no longer needed due to only operator)
     return new IVYActionRefExpression(path, acName, paramsList, false,false);
     }*/
    /**
     * Parse constant, attributes and actions expression
     *
     * @param att
     * @param extraVars
     * @return
     * @throws IVYCompilerException
     */
    private IVYExpression parseAtribbConstActionDefExpression(Matcher att, List<String> extraVars, boolean ctl) throws IVYCompilerException {
        String[] path = (att.group(1).equals("")) ? null : att.group(1).split("\\.");
        String name = att.group(2);
        if (name.matches("[+-]?\\d*")) {
            return new IVYNumberExpression(name);
        }
        if (name.equals("true") || name.equals("false") || this.model.hasConstant(name)) {
            return new IVYConstExpression(name);
        }
        if (name.startsWith("_")) {
            return new IVYAttribExpression(null, name, false);
        }

        String def = this.model.getDefinition(name);
        if (def != null) {
            return new IVYDefRefExpression(name, this.parseExpression(def, extraVars, ctl));
        }

        String indices = att.group(3);
        String primed = att.group(4);
        IVYInteractor interactor = this.model.getInteractor(this.currentInteractor, path);
        IVYAttribute attribDecl = interactor.getAttribute(name);
        if (indices != null) { // Array!
            String[] indexList = indices.split("\\]\\[");
            if (attribDecl == null) {
                throw new IVYCompilerException(0, att.group(0), "Undeclared array attribute.");
            }

            IVYType ivyType = attribDecl.getType();
            if (ivyType == null || !(ivyType instanceof IVYArrayType)) {
                throw new IVYCompilerException(0, att.group(0), "Attribute is not an array.");
            }
            if (ivyType.getDimensions() > indexList.length) {
                throw new IVYCompilerException(0, att.group(0), "Too few dimensions in array.");
            }
            if (ivyType.getDimensions() < indexList.length) {
                throw new IVYCompilerException(0, att.group(0), "Too many dimensions in array.");
            }

            // TODO: validate indices.
            return new IVYArrayAttribExpression(path, name, indexList, (primed != null), this.model);
        }
        if (attribDecl != null || extraVars.contains(name)) {
            IVYType ivyType = null;
            if (attribDecl != null) {
                ivyType = attribDecl.getType();
            }

            return new IVYAttribExpression(path, name, (primed != null), ivyType);
        }

        IVYAction acDecl = interactor.getAction(name);
        if (acDecl != null) {
            return parseSingleActionRefExpression(att, extraVars);
        }

        if (!att.group(1).equals("") || indices != null || primed != null) {
            throw new IVYCompilerException(0, att.group(0), "Undeclared action or attribute.");
        }

        throw new IVYCompilerException(0, att.group(0), "Unknown symbol.");
    }

    /**
     * Parses a binary operator expression
     *
     * @param op the binary operator
     * @param m the ivyMatcher with expression broken into bits (... op ... op
     * ...)
     * @param extraVars variables allowed in the scope of the expression
     * @return the parsed expression
     * @throws IVYCompilerException
     */
    private IVYExpression parseBinaryExpression(String op, IVYMatcher m, List<String> extraVars, boolean ctl)
            throws IVYCompilerException {

        if (m.groupCount() == 2) {
            return new IVYBinaryExpression(op, parseExpression(m.group(1), extraVars, ctl), parseExpression(m.group(2), extraVars, ctl));
        } else {
            IVYExpression expr = parseExpression(m.group(1), extraVars, false);
            m.groupDelete(1);
            return new IVYBinaryExpression(op, expr, parseBinaryExpression(op, m, extraVars, ctl));
        }
    }

    /**
     * Parse inSet expression
     *
     * @param attribName
     * @param values
     * @param extraVars
     * @throws IVYCompilerException
     * @return
     */
    private IVYExpression parseInSetExpression(String attribName, String values, List<String> extraVars) throws IVYCompilerException {
        IVYExpression subexpr = parseExpression(attribName, extraVars, false);
        String[] arrValues = values.split(",");
        IVYConstExpression[] arrExpr = new IVYConstExpression[arrValues.length];

        if (subexpr instanceof IVYAttribExpression) {
            for (int i = 0; i < arrValues.length; i++) {
                IVYExpression expr = this.parseExpression(arrValues[i].trim(), extraVars, false);
                if (!(expr instanceof IVYConstExpression)) {
                    throw new IVYCompilerException(0, arrValues[i].trim(), "Not a valid value in 'in' expression.");
                }
                arrExpr[i] = (IVYConstExpression) expr;
            }
            return new IVYInSetExpression((IVYAttribExpression) subexpr, arrExpr);
        }
        throw new IVYCompilerException(0, attribName, "Not an attribute in 'in' expression.");
    }

    /**
     * Parse inSet expression with a Definition name on the right
     *
     * @param attribName
     * @param values
     * @param extraVars
     * @throws IVYCompilerException
     * @return
     */
    private IVYExpression parseInSetWithDefRefExpression(String attribName, String values, List<String> extraVars) throws IVYCompilerException {
        IVYExpression subexpr = parseExpression(attribName, extraVars, false);

        if (subexpr instanceof IVYAttribExpression) {
            String name = values.trim();
            String def = this.model.getDefinition(name);
            if (def == null) {
                throw new IVYCompilerException(0, name, "Not a definition name in 'in' expression.");
            }
            return new IVYInSetExpression((IVYAttribExpression) subexpr,
                    new IVYDefRefExpression(name, this.parseExpression(def, extraVars, false)));
        }
        throw new IVYCompilerException(0, attribName, "Not an attribute in 'in' expression.");
    }

    /**
     * Parses the keep expressions
     *
     * @param expr
     * @param extraVars
     * @return
     * @throws ivycompiler.IVYCompilerException
     */
    private IVYExpression parseKeepExpression(String expr, List<String> extraVars) throws IVYCompilerException {
        IVYMatcher m = new IVYMatcher(",", expr);
        ArrayList<IVYAttribExpression> list = new ArrayList<IVYAttribExpression>();

        if (!m.matches()) {
            if (!expr.matches("\\s*\\w[^\\s]*\\s*")) {
                throw new IVYCompilerException(0, expr, "Unparsable actions list in keep expression.");
            } else {
                list.addAll(parseKeepParam(expr.trim(), expr.trim(), extraVars));
            }
        } else {
            for (int i = 1; i <= m.groupCount(); i++) {
                list.addAll(parseKeepParam(m.group(0).trim(), m.group(i).trim(), extraVars));
            }
        }

        return new IVYKeepExpression(list);
    }

    /**
     * Parses keep expression parameters. Unfolds array indices as appropriate.
     *
     * @param expr
     * @param at
     * @param extraVars
     * @return
     * @throws ivycompiler.IVYCompilerException
     */
    private List<IVYAttribExpression> parseKeepParam(String expr, String at, List<String> extraVars) throws IVYCompilerException {
        List<IVYAttribExpression> list = new ArrayList<IVYAttribExpression>();
        Matcher atMatcher = Grammar.AttribExpression.matcher(at);
        if (!atMatcher.matches()) {
            throw new IVYCompilerException(0, expr, "Invalid parameter - " + at + " -  in keep expression.");
        }
        IVYAttribExpression atExpr = (IVYAttribExpression) parseAtribbConstActionDefExpression(atMatcher, extraVars, false);
        IVYInteractor inter = this.model.getInteractor(this.currentInteractor, atExpr.getPath());
        IVYAttribute attribDecl = inter.getAttribute(atExpr.getName());
        if (attribDecl == null && !(extraVars.contains(atExpr.getName()) && atExpr.getPath() == null)) {
            throw new IVYCompilerException(0, expr, "Undeclared attribute - " + at + " -  in keep expression.");
        }
        IVYType ivyType = attribDecl.getType();
        if (ivyType instanceof IVYArrayType) {
            List<String> indices = ((IVYArrayType) ivyType).indices();
            for (int i = 0; i < indices.size(); i++) {
                list.add(new IVYAttribExpression(atExpr.getPath(), atExpr.getName() + indices.get(i), false));
            }
        } else {
            list.add(atExpr);
        }

        return list;
    }

    /**
     * Parse U expression
     *
     * @param expr
     * @param extraVars
     * @param op
     * @return
     * @throws IVYCompilerException
     */
    private IVYExpression parseUExpression(String expr, List<String> extraVars, String op) throws IVYCompilerException {
        IVYMatcher matcher = new IVYMatcher("U", expr);
        // TODO: check if CTL is possible in Until
        if (matcher.matches()) {
            return new IVYUnaryExpression(op, parseBinaryExpression("U", matcher, extraVars, false));
        }
        throw new IVYCompilerException(0, expr, "Badly formed until expression.");
    }

    /**
     * Parses the enumeration expressions
     *
     * @param expr
     * @return
     * @throws ivycompiler.IVYCompilerException
     */
    private IVYEnumerationExpression parseEnumerationExpression(String expr) throws IVYCompilerException {
        IVYMatcher m = new IVYMatcher(",", expr);
        List<IVYConstExpression> list = new ArrayList<IVYConstExpression>();
        IVYExpression ivyExpr;
        String def;

        if (!m.matches()) // just one?
        {
            list = parseEnumItemExpression(expr);
        } else {
            for (int i = 1; i <= m.groupCount(); i++) {
                list.addAll(parseEnumItemExpression(m.group(i).trim()));
            }
        }
        return new IVYEnumerationExpression(list);
    }

    /**
     * Parses the enumeration expression items. This method is needed to
     * recursive unravel definitions
     *
     * @param expr
     * @return a list with the constants in the enumeration
     * @throws ivycompiler.IVYCompilerException
     */
    private List<IVYConstExpression> parseEnumItemExpression(String expr) throws IVYCompilerException {
        List<IVYConstExpression> list = new ArrayList<IVYConstExpression>();

        if (!expr.matches("\\s*\\w[^\\s]*\\s*")) {
            throw new IVYCompilerException(0, expr, "Illegal element in enumeration expression.");
        } else {
            if (expr.matches("[+-]?\\d*")) {
                list.add(new IVYNumberExpression(expr));
            } else {
                String def = this.model.getDefinition(expr);
                if (def != null) // TODO: Use IVYDefRefExpression??
                {
                    list = this.parseEnumerationExpression(def).getValues();
                } else {
                    list.add(new IVYConstExpression(expr));
                }
            }
        }
        return list;
    }

    // Code generators

    /*
     * Generate the SMV code for the model
     */
    public String toSMV() throws IVYCompilerException {
        return this.model.toSMV();
    }


    /*
     * Generate the PVS code for the model
     */
    public String toPVS() throws IVYCompilerException {
        return this.model.toPVS();
    }

}
