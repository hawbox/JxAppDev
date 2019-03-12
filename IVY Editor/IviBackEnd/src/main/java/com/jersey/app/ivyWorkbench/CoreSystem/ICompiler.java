
package com.jersey.app.ivyWorkbench.CoreSystem;

import java.util.Scanner;

/**
 *
 * @author jfc
 */
public interface ICompiler {
    public ICompiler compileModel(Scanner scanner) throws IVYCompilerException, Exception;
    public String toSMV() throws IVYCompilerException;
    public String toPVS() throws IVYCompilerException;
}