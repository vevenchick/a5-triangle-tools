package triangle.optimiser;

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.Program;

// Actuals / Formals
import triangle.abstractSyntaxTrees.actuals.*;
import triangle.abstractSyntaxTrees.formals.*;

// Aggregates
import triangle.abstractSyntaxTrees.aggregates.*;

// Commands
import triangle.abstractSyntaxTrees.commands.*;

// Declarations
import triangle.abstractSyntaxTrees.declarations.*;

// Expressions (we count CharacterExpression & IntegerExpression)
import triangle.abstractSyntaxTrees.expressions.*;

// Types
import triangle.abstractSyntaxTrees.types.*;

// Vnames & terminals
import triangle.abstractSyntaxTrees.vnames.*;
import triangle.abstractSyntaxTrees.terminals.*;

// Visitors
import triangle.abstractSyntaxTrees.visitors.*;

/**
 * StatsCounter
 * ----------
 * "ConstantFolder-style" visitor: same traversal structure, but it only counts
 * CharacterExpression and IntegerExpression nodes and leaves the AST unchanged.
 */
public class StatsCounter implements
        ProgramVisitor<Void, AbstractSyntaxTree>,
        CommandVisitor<Void, AbstractSyntaxTree>,
        DeclarationVisitor<Void, AbstractSyntaxTree>,
        ExpressionVisitor<Void, AbstractSyntaxTree>,
        FormalParameterVisitor<Void, AbstractSyntaxTree>,
        FormalParameterSequenceVisitor<Void, AbstractSyntaxTree>,
        ActualParameterVisitor<Void, AbstractSyntaxTree>,
        ActualParameterSequenceVisitor<Void, AbstractSyntaxTree>,
        ArrayAggregateVisitor<Void, AbstractSyntaxTree>,
        RecordAggregateVisitor<Void, AbstractSyntaxTree>,
        TypeDenoterVisitor<Void, AbstractSyntaxTree>,        // also implies FieldTypeDenoterVisitor
        VnameVisitor<Void, AbstractSyntaxTree>,
        IdentifierVisitor<Void, AbstractSyntaxTree>,
        OperatorVisitor<Void, AbstractSyntaxTree>,
        LiteralVisitor<Void, AbstractSyntaxTree> {

    private int numChars = 0;
    private int numInts  = 0;

    public int getNumCharacterExpressions() { return numChars; }
    public int getNumIntegerExpressions()   { return numInts; }

    /* ============================= Program ============================= */

    @Override
    public AbstractSyntaxTree visitProgram(Program ast, Void arg) {
        if (ast != null && ast.C != null) ast.C.visit(this, null);
        return ast;
    }

    /* ============================ Commands ============================ */

    @Override
    public AbstractSyntaxTree visitAssignCommand(AssignCommand ast, Void arg) {
        if (ast.V != null) ast.V.visit(this, null);
        if (ast.E != null) ast.E.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitCallCommand(CallCommand ast, Void arg) {
        if (ast.I  != null) ast.I.visit(this, null);
        if (ast.APS != null) ast.APS.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitEmptyCommand(EmptyCommand ast, Void arg) {
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitIfCommand(IfCommand ast, Void arg) {
        if (ast.E  != null) ast.E.visit(this, null);
        if (ast.C1 != null) ast.C1.visit(this, null);
        if (ast.C2 != null) ast.C2.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitLetCommand(LetCommand ast, Void arg) {
        if (ast.D != null) ast.D.visit(this, null);
        if (ast.C != null) ast.C.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitSequentialCommand(SequentialCommand ast, Void arg) {
        if (ast.C1 != null) ast.C1.visit(this, null);
        if (ast.C2 != null) ast.C2.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitWhileCommand(WhileCommand ast, Void arg) {
        if (ast.E != null) ast.E.visit(this, null);
        if (ast.C != null) ast.C.visit(this, null);
        return ast;
    }

    /* ========================== Declarations ========================== */

    @Override
    public AbstractSyntaxTree visitConstDeclaration(ConstDeclaration ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        if (ast.E != null) ast.E.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitFuncDeclaration(FuncDeclaration ast, Void arg) {
        if (ast.I   != null) ast.I.visit(this, null);
        if (ast.FPS != null) ast.FPS.visit(this, null);
        if (ast.T   != null) ast.T.visit(this, null);
        if (ast.E   != null) ast.E.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitProcDeclaration(ProcDeclaration ast, Void arg) {
        if (ast.I   != null) ast.I.visit(this, null);
        if (ast.FPS != null) ast.FPS.visit(this, null);
        if (ast.C   != null) ast.C.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitSequentialDeclaration(SequentialDeclaration ast, Void arg) {
        if (ast.D1 != null) ast.D1.visit(this, null);
        if (ast.D2 != null) ast.D2.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitTypeDeclaration(TypeDeclaration ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        if (ast.T != null) ast.T.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitVarDeclaration(VarDeclaration ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        if (ast.T != null) ast.T.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Void arg) {
        if (ast.O    != null) ast.O.visit(this, null);
        if (ast.ARG1 != null) ast.ARG1.visit(this, null);
        if (ast.ARG2 != null) ast.ARG2.visit(this, null);
        if (ast.RES  != null) ast.RES.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Void arg) {
        if (ast.O   != null) ast.O.visit(this, null);
        if (ast.ARG != null) ast.ARG.visit(this, null);
        if (ast.RES != null) ast.RES.visit(this, null);
        return ast;
    }

    /* =========================== Expressions ========================== */

    @Override
    public AbstractSyntaxTree visitArrayExpression(ArrayExpression ast, Void arg) {
        if (ast.AA != null) ast.AA.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
        if (ast.E1 != null) ast.E1.visit(this, null);
        if (ast.O  != null) ast.O.visit(this, null);
        if (ast.E2 != null) ast.E2.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitCallExpression(CallExpression ast, Void arg) {
        if (ast.I   != null) ast.I.visit(this, null);
        if (ast.APS != null) ast.APS.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitCharacterExpression(CharacterExpression ast, Void arg) {
        numChars++;
        if (ast.CL != null) ast.CL.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitEmptyExpression(EmptyExpression ast, Void arg) {
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitIfExpression(IfExpression ast, Void arg) {
        if (ast.E1 != null) ast.E1.visit(this, null);
        if (ast.E2 != null) ast.E2.visit(this, null);
        if (ast.E3 != null) ast.E3.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void arg) {
        numInts++;
        if (ast.IL != null) ast.IL.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitLetExpression(LetExpression ast, Void arg) {
        if (ast.D != null) ast.D.visit(this, null);
        if (ast.E != null) ast.E.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitRecordExpression(RecordExpression ast, Void arg) {
        if (ast.RA != null) ast.RA.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitUnaryExpression(UnaryExpression ast, Void arg) {
        if (ast.O != null) ast.O.visit(this, null);
        if (ast.E != null) ast.E.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitVnameExpression(VnameExpression ast, Void arg) {
        if (ast.V != null) ast.V.visit(this, null);
        return ast;
    }

    /* ======================= Formals / Actuals ======================== */

    @Override
    public AbstractSyntaxTree visitConstFormalParameter(ConstFormalParameter ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        if (ast.T != null) ast.T.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Void arg) {
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitFuncFormalParameter(FuncFormalParameter ast, Void arg) {
        if (ast.I   != null) ast.I.visit(this, null);
        if (ast.FPS != null) ast.FPS.visit(this, null);
        if (ast.T   != null) ast.T.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Void arg) {
        if (ast.FP  != null) ast.FP.visit(this, null);
        if (ast.FPS != null) ast.FPS.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitProcFormalParameter(ProcFormalParameter ast, Void arg) {
        if (ast.I   != null) ast.I.visit(this, null);
        if (ast.FPS != null) ast.FPS.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Void arg) {
        if (ast.FP != null) ast.FP.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitVarFormalParameter(VarFormalParameter ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        if (ast.T != null) ast.T.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitConstActualParameter(ConstActualParameter ast, Void arg) {
        if (ast.E != null) ast.E.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Void arg) {
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitFuncActualParameter(FuncActualParameter ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Void arg) {
        if (ast.AP  != null)  ast.AP.visit(this, null);
        if (ast.APS != null) ast.APS.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitProcActualParameter(ProcActualParameter ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitSingleActualParameterSequence(SingleActualParameterSequence ast, Void arg) {
        if (ast.AP != null) ast.AP.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitVarActualParameter(VarActualParameter ast, Void arg) {
        if (ast.V != null) ast.V.visit(this, null);
        return ast;
    }

    /* ========================= Aggregates ========================= */

    @Override
    public AbstractSyntaxTree visitMultipleArrayAggregate(MultipleArrayAggregate ast, Void arg) {
        if (ast.E  != null) ast.E.visit(this, null);
        if (ast.AA != null) ast.AA.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitMultipleRecordAggregate(MultipleRecordAggregate ast, Void arg) {
        if (ast.I  != null) ast.I.visit(this, null);
        if (ast.E  != null) ast.E.visit(this, null);
        if (ast.RA != null) ast.RA.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitSingleArrayAggregate(SingleArrayAggregate ast, Void arg) {
        if (ast.E != null) ast.E.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitSingleRecordAggregate(SingleRecordAggregate ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        if (ast.E != null) ast.E.visit(this, null);
        return ast;
    }

    /* ============================ Types ============================ */

    @Override public AbstractSyntaxTree visitAnyTypeDenoter(AnyTypeDenoter ast, Void arg) { return ast; }

    @Override
    public AbstractSyntaxTree visitArrayTypeDenoter(ArrayTypeDenoter ast, Void arg) {
        if (ast.T  != null) ast.T.visit(this, null);
        if (ast.IL != null) ast.IL.visit(this, null);
        return ast;
    }

    @Override public AbstractSyntaxTree visitBoolTypeDenoter(BoolTypeDenoter ast, Void arg) { return ast; }
    @Override public AbstractSyntaxTree visitCharTypeDenoter(CharTypeDenoter ast, Void arg) { return ast; }
    @Override public AbstractSyntaxTree visitErrorTypeDenoter(ErrorTypeDenoter ast, Void arg) { return ast; }
    @Override public AbstractSyntaxTree visitIntTypeDenoter(IntTypeDenoter ast, Void arg) { return ast; }

    @Override
    public AbstractSyntaxTree visitRecordTypeDenoter(RecordTypeDenoter ast, Void arg) {
        if (ast.FT != null) ast.FT.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitSimpleTypeDenoter(SimpleTypeDenoter ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        return ast;
    }

    /* === FieldTypeDenoterVisitor methods (TypeDenoterVisitor extends it) === */

    @Override
    public AbstractSyntaxTree visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Void arg) {
        if (ast.I  != null) ast.I.visit(this, null);
        if (ast.T  != null) ast.T.visit(this, null);
        if (ast.FT != null) ast.FT.visit(this, null);
        return ast;
    }

    @Override
    public AbstractSyntaxTree visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Void arg) {
        if (ast.I != null) ast.I.visit(this, null);
        if (ast.T != null) ast.T.visit(this, null);
        return ast;
    }

    /* ======================== Vnames / Terminals ======================== */

    @Override public AbstractSyntaxTree visitDotVname(DotVname ast, Void arg)        { if (ast.V!=null) ast.V.visit(this,null); if (ast.I!=null) ast.I.visit(this,null); return ast; }
    @Override public AbstractSyntaxTree visitSimpleVname(SimpleVname ast, Void arg)   { if (ast.I!=null) ast.I.visit(this,null); return ast; }
    @Override public AbstractSyntaxTree visitSubscriptVname(SubscriptVname ast, Void arg) { if (ast.V!=null) ast.V.visit(this,null); if (ast.E!=null) ast.E.visit(this,null); return ast; }

    @Override public AbstractSyntaxTree visitIdentifier(Identifier ast, Void arg)     { return ast; }
    @Override public AbstractSyntaxTree visitOperator(Operator ast, Void arg)         { return ast; }
    @Override public AbstractSyntaxTree visitCharacterLiteral(CharacterLiteral ast, Void arg) { return ast; }
    @Override public AbstractSyntaxTree visitIntegerLiteral(IntegerLiteral ast, Void arg)     { return ast; }
}
