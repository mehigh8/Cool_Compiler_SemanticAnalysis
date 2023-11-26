package cool.compiler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;

public abstract class ASTNode {
    Token start;
    ParserRuleContext ctx;

    ASTNode(ParserRuleContext ctx, Token start) {
        this.ctx = ctx;
        this.start = start;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

class Program extends ASTNode {
    List<Class> classes;
    Program(ParserRuleContext ctx, Token start, List<Class> classes) {
        super(ctx, start);
        this.classes = classes;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Class extends ASTNode {
    Token classId;
    Token parentClassId;
    List<Feature> features;
    Class(ParserRuleContext ctx, Token start, Token classId, Token parentClassId, List<Feature> features) {
        super(ctx, start);
        this.classId = classId;
        this.parentClassId = parentClassId;
        this.features = features;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Feature extends ASTNode {
    Feature(ParserRuleContext ctx, Token start) {
        super(ctx, start);
    }
}

class FuncFeature extends Feature {
    List<Formal> formals;
    Token funcId;
    Token funcType;
    Expression e;
    FuncFeature(ParserRuleContext ctx, Token start, List<Formal> formals, Token funcId, Token funcType, Expression e) {
        super(ctx, start);
        this.formals = formals;
        this.funcId = funcId;
        this.funcType = funcType;
        this.e = e;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class VarFeature extends Feature {
    Token varId;
    Token varType;
    Expression e;
    VarFeature(ParserRuleContext ctx, Token start, Token varId, Token varType, Expression e) {
        super(ctx, start);
        this.varId = varId;
        this.varType = varType;
        this.e = e;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Formal extends ASTNode {
    Token formalId;
    Token formalType;
    Formal(ParserRuleContext ctx, Token start, Token formalId, Token formalType) {
        super(ctx, start);
        this.formalId = formalId;
        this.formalType = formalType;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Expression extends ASTNode {
    Expression(ParserRuleContext ctx, Token start) {
        super(ctx, start);
    }
}

class ExplicitDispatch extends Expression {
    Expression obj;
    Token parentType;
    ImplicitDispatch dispatch;
    ExplicitDispatch(ParserRuleContext ctx, Token start, Expression obj, Token parentType, ImplicitDispatch dispatch) {
        super(ctx, start);
        this.obj = obj;
        this.parentType = parentType;
        this.dispatch = dispatch;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ImplicitDispatch extends Expression {
    Token funcId;
    List<Expression> funcParams;
    boolean fromExplicit;
    ImplicitDispatch(ParserRuleContext ctx, Token start, Token funcId, List<Expression> funcParams, boolean fromExplicit) {
        super(ctx, start);
        this.funcId = funcId;
        this.funcParams = funcParams;
        this.fromExplicit = fromExplicit;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class If extends Expression {
    Expression cond;
    Expression thenBranch;
    Expression elseBranch;
    If(ParserRuleContext ctx, Token start, Expression cond, Expression thenBranch, Expression elseBranch) {
        super(ctx, start);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class While extends Expression {
    Expression cond;
    Expression content;
    While(ParserRuleContext ctx, Token start, Expression cond, Expression content) {
        super(ctx, start);
        this.cond = cond;
        this.content = content;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    List<Expression> exprs;
    Block(ParserRuleContext ctx, Token start, List<Expression> exprs) {
        super(ctx, start);
        this.exprs = exprs;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Local extends ASTNode {
    Token varId;
    Token varType;
    Expression varExpr;
    Local(ParserRuleContext ctx, Token start, Token varId, Token varType, Expression varExpr) {
        super(ctx, start);
        this.varId = varId;
        this.varType = varType;
        this.varExpr = varExpr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    List<Local> localList;
    Expression letContent;
    Let(ParserRuleContext ctx, Token start, List<Local> localList, Expression letContent) {
        super(ctx, start);
        this.localList = localList;
        this.letContent = letContent;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class CaseBranch extends ASTNode {
    Token varId;
    Token varType;
    Expression branchExpr;
    CaseBranch(ParserRuleContext ctx, Token start, Token varId, Token varType, Expression branchExpr) {
        super(ctx, start);
        this.varId = varId;
        this.varType = varType;
        this.branchExpr = branchExpr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Case extends Expression {
    Expression caseExpr;
    List<CaseBranch> caseBranches;
    Case(ParserRuleContext ctx, Token start, Expression caseExpr, List<CaseBranch> caseBranches) {
        super(ctx, start);
        this.caseExpr = caseExpr;
        this.caseBranches = caseBranches;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class New extends Expression {
    Token initType;
    New(ParserRuleContext ctx, Token start, Token initType) {
        super(ctx, start);
        this.initType = initType;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Compl extends Expression {
    Expression e;
    Compl(ParserRuleContext ctx, Token start, Expression e) {
        super(ctx, start);
        this.e = e;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Isvoid extends Expression {
    Expression e;
    Isvoid(ParserRuleContext ctx, Token start, Expression e) {
        super(ctx, start);
        this.e = e;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class MultDiv extends Expression {
    Expression left;
    Expression right;
    Token op;
    MultDiv(ParserRuleContext ctx, Token start, Expression left, Expression right, Token op) {
        super(ctx, start);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class PlusMinus extends Expression {
    Expression left;
    Expression right;
    Token op;
    PlusMinus(ParserRuleContext ctx, Token start, Expression left, Expression right, Token op) {
        super(ctx, start);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Relational extends Expression {
    Expression left;
    Expression right;
    Token op;
    Relational(ParserRuleContext ctx, Token start, Expression left, Expression right, Token op) {
        super(ctx, start);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Not extends Expression {
    Expression e;
    Not(ParserRuleContext ctx, Token start, Expression e) {
        super(ctx, start);
        this.e = e;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Assign extends Expression {
    Token varId;
    Expression e;
    Assign(ParserRuleContext ctx, Token start, Token varId, Expression e) {
        super(ctx, start);
        this.varId = varId;
        this.e = e;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Paren extends Expression {
    Expression e;
    Paren(ParserRuleContext ctx, Token start, Expression e) {
        super(ctx, start);
        this.e = e;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Id extends Expression {
    Token varId;
    Id(ParserRuleContext ctx, Token start, Token varId) {
        super(ctx, start);
        this.varId = varId;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class IntegerExpr extends Expression {
    IntegerExpr(ParserRuleContext ctx, Token start) {
        super(ctx, start);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


class StringExpr extends Expression {
    StringExpr(ParserRuleContext ctx, Token start) {
        super(ctx, start);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class BoolExpr extends Expression {
    BoolExpr(ParserRuleContext ctx, Token start) {
        super(ctx, start);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
