package cool.compiler;

import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;

import java.util.stream.Collectors;

public class ASTConstructionVisitor extends CoolParserBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        return new Program(ctx,
                ctx.start,
                ctx.classes.stream().map(cls -> (Class)visit(cls)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitClass(CoolParser.ClassContext ctx) {
        return new Class(ctx,
                ctx.start,
                ctx.classId,
                ctx.parentClassId,
                ctx.features.stream().map(ftr -> (Feature)visit(ftr)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitFuncFeature(CoolParser.FuncFeatureContext ctx) {
        return new FuncFeature(ctx,
                ctx.start,
                ctx.formals.stream().map(frml -> (Formal)visit(frml)).collect(Collectors.toList()),
                ctx.funcId,
                ctx.funcType,
                (Expression) visit(ctx.e));
    }

    @Override
    public ASTNode visitVarFeature(CoolParser.VarFeatureContext ctx) {
        return new VarFeature(ctx,
                ctx.start,
                ctx.varId,
                ctx.varType,
                (ctx.e == null) ? null : (Expression) visit(ctx.e));
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        return new Formal(ctx,
                ctx.start,
                ctx.formalId,
                ctx.formalType);
    }

    @Override
    public ASTNode visitExplicitDispatch(CoolParser.ExplicitDispatchContext ctx) {
        return new ExplicitDispatch(ctx,
                ctx.start,
                (Expression) visit(ctx.obj),
                ctx.parentType,
                new ImplicitDispatch(ctx,
                        ctx.start,
                        ctx.funcId,
                        ctx.funcParams.stream().map(fp -> (Expression)visit(fp)).collect(Collectors.toList()),
                        true));
    }

    @Override
    public ASTNode visitImplicitDispatch(CoolParser.ImplicitDispatchContext ctx) {
        return new ImplicitDispatch(ctx,
                ctx.start,
                ctx.funcId,
                ctx.funcParams.stream().map(fp -> (Expression)visit(fp)).collect(Collectors.toList()),
                false);
    }

    @Override
    public ASTNode visitIf(CoolParser.IfContext ctx) {
        return new If(ctx,
                ctx.start,
                (Expression)visit(ctx.cond),
                (Expression)visit(ctx.thenBranch),
                (Expression)visit(ctx.elseBranch));
    }

    @Override
    public ASTNode visitWhile(CoolParser.WhileContext ctx) {
        return new While(ctx,
                ctx.start,
                (Expression)visit(ctx.cond),
                (Expression)visit(ctx.content));
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        return new Block(ctx,
                ctx.start,
                ctx.exprs.stream().map(expr -> (Expression)visit(expr)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitLocal(CoolParser.LocalContext ctx) {
        return new Local(ctx,
                ctx.start,
                ctx.varId,
                ctx.varType,
                (ctx.varExpr == null) ? null : (Expression)visit(ctx.varExpr));
    }

    @Override
    public ASTNode visitLet(CoolParser.LetContext ctx) {
        return new Let(ctx,
                ctx.start,
                ctx.localList.stream().map(local -> (Local)visit(local)).collect(Collectors.toList()),
                (Expression)visit(ctx.letContent));
    }

    @Override
    public ASTNode visitCaseBranch(CoolParser.CaseBranchContext ctx) {
        return new CaseBranch(ctx,
                ctx.start,
                ctx.varId,
                ctx.varType,
                (Expression)visit(ctx.branchExpr));
    }

    @Override
    public ASTNode visitCase(CoolParser.CaseContext ctx) {
        return new Case(ctx,
                ctx.start,
                (Expression)visit(ctx.caseExpr),
                ctx.caseBranches.stream().map(cbr -> (CaseBranch)visit(cbr)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitNew(CoolParser.NewContext ctx) {
        return new New(ctx,
                ctx.start,
                ctx.initType);
    }

    @Override
    public ASTNode visitCompl(CoolParser.ComplContext ctx) {
        return new Compl(ctx,
                ctx.start,
                (Expression)visit(ctx.e));
    }

    @Override
    public ASTNode visitIsvoid(CoolParser.IsvoidContext ctx) {
        return new Isvoid(ctx,
                ctx.start,
                (Expression)visit(ctx.e));
    }

    @Override
    public ASTNode visitMultDiv(CoolParser.MultDivContext ctx) {
        return new MultDiv(ctx,
                ctx.start,
                (Expression)visit(ctx.left),
                (Expression)visit(ctx.right),
                ctx.op);
    }

    @Override
    public ASTNode visitPlusMinus(CoolParser.PlusMinusContext ctx) {
        return new PlusMinus(ctx,
                ctx.start,
                (Expression)visit(ctx.left),
                (Expression)visit(ctx.right),
                ctx.op);
    }

    @Override
    public ASTNode visitRelational(CoolParser.RelationalContext ctx) {
        return new Relational(ctx,
                ctx.start,
                (Expression)visit(ctx.left),
                (Expression)visit(ctx.right),
                ctx.op);
    }

    @Override
    public ASTNode visitNot(CoolParser.NotContext ctx) {
        return new Not(ctx,
                ctx.start,
                (Expression)visit(ctx.e));
    }

    @Override
    public ASTNode visitAssign(CoolParser.AssignContext ctx) {
        return new Assign(ctx,
                ctx.start,
                ctx.varId,
                (Expression)visit(ctx.e));
    }

    @Override
    public ASTNode visitParen(CoolParser.ParenContext ctx) {
        return new Paren(ctx,
                ctx.start,
                (Expression)visit(ctx.e));
    }

    @Override
    public ASTNode visitId(CoolParser.IdContext ctx) {
        return new Id(ctx,
                ctx.start,
                ctx.varId);
    }

    @Override
    public ASTNode visitInteger(CoolParser.IntegerContext ctx) {
        return new IntegerExpr(ctx,
                ctx.start);
    }

    @Override
    public ASTNode visitString(CoolParser.StringContext ctx) {
        return new StringExpr(ctx,
                ctx.start);
    }

    @Override
    public ASTNode visitBool(CoolParser.BoolContext ctx) {
        return new BoolExpr(ctx,
                ctx.start);
    }
}
