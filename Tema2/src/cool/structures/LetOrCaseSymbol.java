package cool.structures;

public class LetOrCaseSymbol extends IdSymbol implements Scope {
    private Scope parent;
    public LetOrCaseSymbol(String name, Scope parent) {
        super(name);
        this.parent = parent;
    }

    @Override
    public boolean add(Symbol sym) {
        return false;
    }

    @Override
    public Symbol lookup(String str) {
        if (str.substring(1).equals(name))
            return this;

        if (parent != null)
            return parent.lookup(str);

        return null;
    }

    @Override
    public Scope getParent() {
        return parent;
    }
}
