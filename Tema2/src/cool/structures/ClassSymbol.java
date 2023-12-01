package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassSymbol extends Symbol implements Scope {
    private Scope parent;
    private Map<String, IdSymbol> attributes = new LinkedHashMap<>();
    private Map<String, FunctionSymbol> methods = new LinkedHashMap<>();

    private ClassSymbol inheritedClass;

    public static final String[] illegalParents = {"Int", "String", "Bool", "SELF_TYPE"};

    public ClassSymbol(String name, Scope parent, ClassSymbol inheritedClass) {
        super(name);
        this.parent = parent;
        this.inheritedClass = inheritedClass;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    @Override
    public boolean add(Symbol sym) {
        if (sym instanceof FunctionSymbol)
            return addMethod((FunctionSymbol) sym);

        if (sym instanceof IdSymbol)
            return addAttribute((IdSymbol) sym);

        return false;
    }

    public boolean hasAttribute(IdSymbol sym) {
        if (attributes.containsKey(sym.getName()))
            return true;

        return inheritedClass != null && inheritedClass.hasAttribute(sym);
    }

    public boolean addAttribute(IdSymbol sym) {
        if (hasAttribute(sym))
            return false;

        attributes.put(sym.getName(), sym);

        return true;
    }

    public boolean hasMethod(FunctionSymbol sym) {
        if (methods.containsKey(sym.getName()))
            return true;

        return inheritedClass != null && inheritedClass.hasMethod(sym);
    }

    public boolean addMethod(FunctionSymbol sym) {
        if (hasMethod(sym))
            return false;

        methods.put(sym.getName(), sym);

        return true;
    }

    @Override
    public Symbol lookup(String str) {
        char type = str.charAt(0);
        if (type == '1')
            return lookupAttribute(str.substring(1));

        if (type == '2')
            return lookupMethod(str.substring(1));

        if (parent != null)
            return parent.lookup(str.substring(1));

        return null;
    }

    public Symbol lookupAttribute(String str) {
        var sym = attributes.get(str);

        if (sym != null)
            return sym;

        if (inheritedClass != null) {
            var inhSym = inheritedClass.lookup("1" + str);
            if (inhSym != null)
                return inhSym;
        }

        if (parent != null)
            return parent.lookup(str);

        return null;
    }

    public Symbol lookupMethod(String str) {
        var sym = methods.get(str);

        if (sym != null)
            return sym;

        if (inheritedClass != null) {
            var inhSym = inheritedClass.lookup("2" + str);
            if (inhSym != null)
                return inhSym;
        }

        if (parent != null)
            return parent.lookup(str);

        return null;
    }

    public Map<String, IdSymbol> getAttributes() {
        return attributes;
    }

    public Map<String, FunctionSymbol> getMethods() {
        return methods;
    }

    public ClassSymbol getInheritedClass() {
        return inheritedClass;
    }

    public void setInheritedClass(ClassSymbol inheritedClass) {
        this.inheritedClass = inheritedClass;
    }
}
