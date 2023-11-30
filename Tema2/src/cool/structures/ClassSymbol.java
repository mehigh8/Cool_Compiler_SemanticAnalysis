package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassSymbol extends Symbol implements Scope {
    private Scope parent;
    private Map<String, Symbol> symbols = new LinkedHashMap<>();

    private ClassSymbol inheritedClass;

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
        if (symbols.containsKey(sym.getName()))
            return false;

        if (inheritedClass != null)
            if (inheritedClass.getSymbols().containsKey(sym.getName()))
                return false;

        symbols.put(sym.getName(), sym);

        return true;
    }

    @Override
    public Symbol lookup(String str) {
        var sym = symbols.get(str);

        if (sym != null)
            return sym;

        if (inheritedClass != null) {
            sym = inheritedClass.getSymbols().get(str);
            if (sym != null)
                return sym;
        }

        if (parent != null)
            return parent.lookup(str);

        return null;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }

    public ClassSymbol getInheritedClass() {
        return inheritedClass;
    }

    public void setInheritedClass(ClassSymbol inheritedClass) {
        this.inheritedClass = inheritedClass;
    }
}
