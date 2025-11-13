package triangle.optimiser;

import java.util.*;
import triangle.abstractSyntaxTrees.Program;
import triangle.abstractSyntaxTrees.commands.*;
import triangle.abstractSyntaxTrees.declarations.*;
import triangle.abstractSyntaxTrees.expressions.*;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.vnames.*;
import triangle.syntacticAnalyzer.SourcePosition;

/**
 * While-loop hoisting (loop-invariant code motion).
 *
 * Rewrites invariant assignments inside a while-body:
 * b := E (E side-effect-free and uses no vars assigned in loop)
 * →
 * let const $hN ~ E in while ... do ...; b := $hN; ...
 *
 * Conservative and AST-name agnostic (only common node types are referenced).
 */
public final class HoistingOptimiser {

    /**
     * Entry point: return the transformed program (or the original if no change).
     */
    public Program optimise(Program prog) {
        if (prog == null || prog.C == null)
            return prog;
        Command newC = hoistInCommand(prog.C);
        if (newC == prog.C)
            return prog;
        return new Program(newC, new SourcePosition());
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // Traversal over commands
    //
    ///////////////////////////////////////////////////////////////////////////////
    private Command hoistInCommand(Command c) {
        if (c == null)
            return null;

        if (c instanceof WhileCommand) {
            return hoistInWhile((WhileCommand) c);
        } else if (c instanceof IfCommand) {
            IfCommand ic = (IfCommand) c;
            Command t = hoistInCommand(ic.C1);
            Command e = hoistInCommand(ic.C2);
            if (t == ic.C1 && e == ic.C2)
                return c;
            return new IfCommand(ic.E, t, e, pos());
        } else if (c instanceof LetCommand) {
            LetCommand lc = (LetCommand) c;
            Command body = hoistInCommand(lc.C);
            if (body == lc.C)
                return c;
            return new LetCommand(lc.D, body, pos());
        } else if (c instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) c;
            Command c1 = hoistInCommand(sc.C1);
            Command c2 = hoistInCommand(sc.C2);
            if (c1 == sc.C1 && c2 == sc.C2)
                return c;
            return new SequentialCommand(c1, c2, pos());
        }
        // AssignCommand, CallCommand, SkipCommand, etc. unchanged
        return c;
    }

    private Command hoistInWhile(WhileCommand wc) {
        // 1) Collect variables assigned anywhere in the while body
        Set<String> assigned = new HashSet<>();
        collectAssignedIds(wc.C, assigned);

        // 2) Replace invariant RHS with hoisted consts
        List<Declaration> hoistedDecls = new ArrayList<>();
        NameSupply names = new NameSupply();
        Command body2 = replaceWithHoistedTemps(wc.C, assigned, hoistedDecls, names);

        // Recurse for nested loops/ifs after the replacements
        body2 = hoistInCommand(body2);

        if (hoistedDecls.isEmpty()) {
            if (body2 == wc.C)
                return wc;
            return new WhileCommand(wc.E, body2, pos());
        }

        Declaration chained = chain(hoistedDecls);
        WhileCommand newWhile = new WhileCommand(wc.E, body2, pos());
        return new LetCommand(chained, newWhile, pos());
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // Pass 1: variables assigned in loop
    //
    ///////////////////////////////////////////////////////////////////////////////
    private void collectAssignedIds(Command c, Set<String> out) {
        if (c == null)
            return;

        if (c instanceof AssignCommand) {
            AssignCommand ac = (AssignCommand) c;
            String id = baseIdent(ac.V);
            if (id != null)
                out.add(id);
        } else if (c instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) c;
            collectAssignedIds(sc.C1, out);
            collectAssignedIds(sc.C2, out);
        } else if (c instanceof IfCommand) {
            IfCommand ic = (IfCommand) c;
            collectAssignedIds(ic.C1, out);
            collectAssignedIds(ic.C2, out);
        } else if (c instanceof WhileCommand) {
            collectAssignedIds(((WhileCommand) c).C, out);
        } else if (c instanceof LetCommand) {
            collectAssignedIds(((LetCommand) c).C, out);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // Pass 2: perform replacements and record hoisted decls
    //
    ///////////////////////////////////////////////////////////////////////////////
    private Command replaceWithHoistedTemps(
            Command c, Set<String> assignedInLoop, List<Declaration> hoisted, NameSupply names) {

        if (c == null)
            return null;

        if (c instanceof AssignCommand) {
            AssignCommand ac = (AssignCommand) c;
            Expression rhs = ac.E;

            if (isSideEffectFree(rhs) && isInvariant(rhs, assignedInLoop)) {
                String fresh = names.fresh();
                Identifier id = new Identifier(fresh, pos());
                ConstDeclaration cd = new ConstDeclaration(id, rhs, pos());
                hoisted.add(cd);

                Vname hv = new SimpleVname(id, pos());
                VnameExpression hvExpr = new VnameExpression(hv, pos());
                return new AssignCommand(ac.V, hvExpr, pos());
            }
            return ac;

        } else if (c instanceof SequentialCommand) {
            SequentialCommand sc = (SequentialCommand) c;
            Command c1 = replaceWithHoistedTemps(sc.C1, assignedInLoop, hoisted, names);
            Command c2 = replaceWithHoistedTemps(sc.C2, assignedInLoop, hoisted, names);
            if (c1 == sc.C1 && c2 == sc.C2)
                return c;
            return new SequentialCommand(c1, c2, pos());

        } else if (c instanceof IfCommand) {
            IfCommand ic = (IfCommand) c;
            Command t = replaceWithHoistedTemps(ic.C1, assignedInLoop, hoisted, names);
            Command e = replaceWithHoistedTemps(ic.C2, assignedInLoop, hoisted, names);
            if (t == ic.C1 && e == ic.C2)
                return c;
            return new IfCommand(ic.E, t, e, pos());

        } else if (c instanceof WhileCommand) {
            WhileCommand inner = (WhileCommand) c;
            Command b = replaceWithHoistedTemps(inner.C, assignedInLoop, hoisted, names);
            if (b == inner.C)
                return c;
            return new WhileCommand(inner.E, b, pos());

        } else if (c instanceof LetCommand) {
            LetCommand lc = (LetCommand) c;
            Command b = replaceWithHoistedTemps(lc.C, assignedInLoop, hoisted, names);
            if (b == lc.C)
                return c;
            return new LetCommand(lc.D, b, pos());
        }

        return c;
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // Invariance & purity
    //
    ///////////////////////////////////////////////////////////////////////////////

    private boolean isInvariant(Expression e, Set<String> assignedInLoop) {
        Set<String> fv = new HashSet<>();
        collectFreeVars(e, fv);
        for (String v : fv)
            if (assignedInLoop.contains(v))
                return false;
        return true;
    }

    private void collectFreeVars(Expression e, Set<String> out) {
        if (e == null)
            return;

        if (e instanceof VnameExpression) {
            String id = baseIdent(((VnameExpression) e).V);
            if (id != null)
                out.add(id);
            return;
        }
        if (e instanceof UnaryExpression) {
            collectFreeVars(((UnaryExpression) e).E, out);
            return;
        }
        if (e instanceof BinaryExpression) {
            BinaryExpression b = (BinaryExpression) e;
            collectFreeVars(b.E1, out);
            collectFreeVars(b.E2, out);
            return;
        }
        if (e instanceof IfExpression) {
            IfExpression ie = (IfExpression) e;
            collectFreeVars(ie.E1, out);
            collectFreeVars(ie.E2, out);
            collectFreeVars(ie.E3, out);
            return;
        }
        if (e instanceof LetExpression) {
            LetExpression le = (LetExpression) e;
            // conservative: don’t bind; just traverse the value expression
            collectFreeVars(le.E, out);
        }
        // literal wrappers are leaves → nothing to add
    }

    private boolean isSideEffectFree(Expression e) {
        if (e == null)
            return true;

        // Accept common literal wrappers without hard-coding their package
        String simple = e.getClass().getSimpleName();
        if (simple.equals("IntegerExpression") ||
                simple.equals("CharacterExpression") ||
                simple.equals("BooleanExpression") || // some codebases use this
                simple.equals("EmptyExpression")) {
            return true;
        }

        if (e instanceof VnameExpression)
            return true;
        if (e instanceof UnaryExpression)
            return isSideEffectFree(((UnaryExpression) e).E);
        if (e instanceof BinaryExpression) {
            BinaryExpression b = (BinaryExpression) e;
            return isSideEffectFree(b.E1) && isSideEffectFree(b.E2);
        }

        // Be conservative about calls/lets/unknown kinds
        if (e instanceof CallExpression)
            return false;
        if (e instanceof LetExpression)
            return false;

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // Utilities
    //
    ///////////////////////////////////////////////////////////////////////////////
    /** Base identifier of a Vname (a, a[i], a.b → "a"). */
    private String baseIdent(Vname v) {
        if (v == null)
            return null;
        if (v instanceof SimpleVname)
            return ((SimpleVname) v).I.spelling;
        if (v instanceof DotVname)
            return baseIdent(((DotVname) v).V);
        if (v instanceof SubscriptVname)
            return baseIdent(((SubscriptVname) v).V);
        return null;
    }

    private Declaration chain(List<Declaration> ds) {
        if (ds.isEmpty())
            return null;
        Declaration acc = ds.get(0);
        for (int i = 1; i < ds.size(); i++) {
            acc = new SequentialDeclaration(acc, ds.get(i), pos());
        }
        return acc;
    }

    private SourcePosition pos() {
        return new SourcePosition();
    }

    private static final class NameSupply {
        private int n = 0;

        String fresh() {
            return "$h" + (++n);
        }
    }
}
