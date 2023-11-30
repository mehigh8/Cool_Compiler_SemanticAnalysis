package cool.structures;

import java.io.File;

import org.antlr.v4.runtime.*;

import cool.compiler.Compiler;
import cool.parser.CoolParser;

public class SymbolTable {
    public static Scope globals;
    
    private static boolean semanticErrors;
    
    public static void defineBasicClasses() {
        globals = new DefaultScope(null);
        semanticErrors = false;

        // Defining basic classes
        ClassSymbol object = new ClassSymbol("Object", globals, null);
        ClassSymbol string = new ClassSymbol("String", globals, object);
        ClassSymbol intt = new ClassSymbol("Int", globals, object);
        ClassSymbol bool = new ClassSymbol("Bool", globals, object);
        ClassSymbol io = new ClassSymbol("IO", globals, object);

        // Defining basic functions and adding them to each class
        // Object
        FunctionSymbol abort = new FunctionSymbol("abort", object);
        abort.setType(object);
        object.add(abort);

        FunctionSymbol type_name = new FunctionSymbol("type_name", object);
        abort.setType(string);
        object.add(type_name);

        FunctionSymbol copy = new FunctionSymbol("copy", object);
        copy.setType(object); // TODO: SELF_TYPE
        object.add(copy);

        // String
        FunctionSymbol length = new FunctionSymbol("length", string);
        length.setType(intt);
        string.add(length);

        FunctionSymbol concat = new FunctionSymbol("concat", string);
        concat.setType(string);
        IdSymbol concatFormal = new IdSymbol("s");
        concatFormal.setType(string);
        concat.add(concatFormal);
        string.add(concat);

        FunctionSymbol substr = new FunctionSymbol("substr", string);
        substr.setType(string);
        IdSymbol substrFormal1 = new IdSymbol("i");
        substrFormal1.setType(intt);
        IdSymbol substrFormal2 = new IdSymbol("l");
        substrFormal2.setType(intt);
        substr.add(substrFormal1);
        substr.add(substrFormal2);
        string.add(substr);

        // IO
        FunctionSymbol out_string = new FunctionSymbol("out_string", io);
        out_string.setType(io); // TODO: SELF_TYPE
        IdSymbol out_stringFormal = new IdSymbol("x");
        out_stringFormal.setType(string);
        out_string.add(out_stringFormal);
        io.add(out_string);

        FunctionSymbol out_int = new FunctionSymbol("out_string", io);
        out_int.setType(io); // TODO: SELF_TYPE
        IdSymbol out_intFormal = new IdSymbol("x");
        out_intFormal.setType(intt);
        out_int.add(out_intFormal);
        io.add(out_int);

        FunctionSymbol in_string = new FunctionSymbol("in_string", io);
        in_string.setType(string);
        io.add(in_string);

        FunctionSymbol in_int = new FunctionSymbol("in_int", io);
        in_int.setType(intt);
        io.add(in_int);

        // Adding the basic classes to the global scope
        globals.add(object);
        globals.add(string);
        globals.add(intt);
        globals.add(bool);
        globals.add(io);
    }
    
    /**
     * Displays a semantic error message.
     * 
     * @param ctx Used to determine the enclosing class context of this error,
     *            which knows the file name in which the class was defined.
     * @param info Used for line and column information.
     * @param str The error message.
     */
    public static void error(ParserRuleContext ctx, Token info, String str) {
        while (! (ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();
        
        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static void error(String str) {
        String message = "Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static boolean hasSemanticErrors() {
        return semanticErrors;
    }
}
