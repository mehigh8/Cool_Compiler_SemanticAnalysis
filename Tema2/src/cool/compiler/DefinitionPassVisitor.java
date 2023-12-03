package cool.compiler;

import cool.structures.*;

import java.util.Arrays;

public class DefinitionPassVisitor implements ASTVisitor<Void> {
    private Scope currentScope;
    @Override
    public Void visit(Program program) {
        currentScope = SymbolTable.globals;
        program.classes.forEach(cls -> cls.accept(this));
        return null;
    }

    @Override
    public Void visit(Class classs) {
        if (classs.classId.getText().equals("SELF_TYPE")) {
            SymbolTable.error(classs.ctx, classs.classId, "Class has illegal name SELF_TYPE");
            return null;
        }

        if (currentScope.lookup(classs.classId.getText()) != null) {
            SymbolTable.error(classs.ctx, classs.classId, "Class " + classs.classId.getText() + " is redefined");
            return null;
        }

        if (classs.parentClassId != null && Arrays.stream(ClassSymbol.illegalParents).toList().contains(classs.parentClassId.getText())) {
            SymbolTable.error(classs.ctx, classs.parentClassId, "Class " + classs.classId.getText() + " has illegal parent " + classs.parentClassId.getText());
        }

        ClassSymbol symbol = new ClassSymbol(classs.classId.getText(), currentScope, null);
        classs.setSymbol(symbol);

        currentScope.add(symbol);
        currentScope = symbol;

        classs.features.forEach(feature -> feature.accept(this));

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(FuncFeature funcFeature) {
        ClassSymbol classSymbol = (ClassSymbol) currentScope;
        funcFeature.setParentClass(classSymbol.getName());

        if (currentScope.lookup("2" + funcFeature.funcId.getText()) != null) {
            SymbolTable.error(funcFeature.ctx, funcFeature.start, "Class " + funcFeature.parentClass + " redefines method " + funcFeature.funcId.getText());
            return null;
        }

        FunctionSymbol symbol = new FunctionSymbol(funcFeature.funcId.getText(), currentScope);
        funcFeature.setSymbol(symbol);

        currentScope.add(symbol);
        currentScope = symbol;

        funcFeature.formals.forEach(formal -> formal.accept(this));
        funcFeature.e.accept(this);

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(VarFeature varFeature) {
        ClassSymbol classSymbol = (ClassSymbol) currentScope;
        varFeature.setParentClass(classSymbol.getName());

        if (varFeature.varId.getText().equals("self")) {
            SymbolTable.error(varFeature.ctx, varFeature.start, "Class " + varFeature.parentClass + " has attribute with illegal name self");
            return null;
        }
        if (currentScope.lookup("1" + varFeature.varId.getText()) != null) {
            SymbolTable.error(varFeature.ctx, varFeature.start, "Class " + varFeature.parentClass + " redefines attribute " + varFeature.varId.getText());
            return null;
        }

        IdSymbol symbol = new IdSymbol(varFeature.varId.getText());
        varFeature.setSymbol(symbol);

        currentScope.add(symbol);

        if (varFeature.e != null)
            varFeature.e.accept(this);

        return null;
    }

    @Override
    public Void visit(Formal formal) {
        FunctionSymbol functionSymbol = (FunctionSymbol) currentScope;
        formal.setParentMethod(functionSymbol.getName());
        ClassSymbol classSymbol = (ClassSymbol) functionSymbol.getParent();
        formal.setParentClass(classSymbol.getName());

        if (formal.formalId.getText().equals("self")) {
            SymbolTable.error(formal.ctx, formal.start, "Method " + formal.parentMethod + " of class " + formal.parentClass + " has formal parameter with illegal name self");
            return null;
        }

        if (formal.formalType.getText().equals("SELF_TYPE")) {
            SymbolTable.error(formal.ctx, formal.formalType, "Method " + formal.parentMethod + " of class " + formal.parentClass + " has formal parameter " + formal.formalId.getText() + " with illegal type SELF_TYPE");
        }

        IdSymbol symbol = new IdSymbol(formal.formalId.getText());
        if (!currentScope.add(symbol)) {
            SymbolTable.error(formal.ctx, formal.start, "Method " + formal.parentMethod + " of class " + formal.parentClass + " redefines formal parameter " + formal.formalId.getText());
            return null;
        }

        formal.setSymbol(symbol);

        return null;
    }

    @Override
    public Void visit(ExplicitDispatch explicitDispatch) {
        if (explicitDispatch.parentType != null && explicitDispatch.parentType.getText().equals("SELF_TYPE")) {
            SymbolTable.error(explicitDispatch.ctx, explicitDispatch.parentType, "Type of static dispatch cannot be SELF_TYPE");
            return null;
        }

        explicitDispatch.obj.accept(this);
        explicitDispatch.dispatch.accept(this);

        return null;
    }

    @Override
    public Void visit(ImplicitDispatch implicitDispatch) {
        implicitDispatch.funcParams.forEach(param -> param.accept(this));
        return null;
    }

    @Override
    public Void visit(If iff) {
        iff.cond.accept(this);
        iff.thenBranch.accept(this);
        iff.elseBranch.accept(this);
        return null;
    }

    @Override
    public Void visit(While whilee) {
        whilee.cond.accept(this);
        whilee.content.accept(this);
        return null;
    }

    @Override
    public Void visit(Block block) {
        block.exprs.forEach(expr -> expr.accept(this));
        return null;
    }

    @Override
    public Void visit(Local local) {
        if (local.varId.getText().equals("self")) {
            SymbolTable.error(local.ctx, local.start, "Let variable has illegal name self");
            return null;
        }

        LetOrCaseSymbol symbol = new LetOrCaseSymbol(local.varId.getText(), currentScope);
        local.setSymbol(symbol);

        if (local.varExpr != null)
            local.varExpr.accept(this);

        currentScope = symbol;
        return null;
    }

    @Override
    public Void visit(Let let) {
        Scope beforeLet = currentScope;

        let.localList.forEach(local -> local.accept(this));
        let.letContent.accept(this);

        currentScope = beforeLet;
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
        if (caseBranch.varId.getText().equals("self")) {
            SymbolTable.error(caseBranch.ctx, caseBranch.start, "Case variable has illegal name self");
            return null;
        }

        if (caseBranch.varType.getText().equals("SELF_TYPE")) {
            SymbolTable.error(caseBranch.ctx, caseBranch.varType, "Case variable " + caseBranch.varId.getText() + " has illegal type SELF_TYPE");
            return null;
        }

        LetOrCaseSymbol symbol = new LetOrCaseSymbol(caseBranch.varId.getText(), currentScope);
        caseBranch.setSymbol(symbol);

        currentScope = symbol;

        caseBranch.branchExpr.accept(this);

        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(Case casee) {
        casee.caseExpr.accept(this);
        casee.caseBranches.forEach(branch -> branch.accept(this));
        return null;
    }

    @Override
    public Void visit(New neww) {
        return null;
    }

    @Override
    public Void visit(Compl compl) {
        compl.e.accept(this);
        return null;
    }

    @Override
    public Void visit(Isvoid isvoid) {
        isvoid.e.accept(this);
        return null;
    }

    @Override
    public Void visit(MultDiv multDiv) {
        multDiv.left.accept(this);
        multDiv.right.accept(this);
        return null;
    }

    @Override
    public Void visit(PlusMinus plusMinus) {
        plusMinus.left.accept(this);
        plusMinus.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Relational relational) {
        relational.left.accept(this);
        relational.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Not not) {
        not.e.accept(this);
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        if (assign.varId.getText().equals("self")) {
            SymbolTable.error(assign.ctx, assign.start, "Cannot assign to self");
            return null;
        }

        assign.e.accept(this);
        return null;
    }

    @Override
    public Void visit(Paren paren) {
        paren.e.accept(this);
        return null;
    }

    @Override
    public Void visit(Id id) {
        return null;
    }

    @Override
    public Void visit(IntegerExpr integerExpr) {
        return null;
    }

    @Override
    public Void visit(StringExpr stringExpr) {
        return null;
    }

    @Override
    public Void visit(BoolExpr boolExpr) {
        return null;
    }
}
