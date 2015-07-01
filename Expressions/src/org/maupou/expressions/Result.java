package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice Maupou
 */
public class Result {

    private final String result;
    private final HashMap<Expression, Expression> changes;
    private final String name;
    private final String ref;
    private int level;

    /**
     *
     * @param e
     * @param syntax
     * @throws Exception
     */
    public Result(Element e, Syntax syntax) throws Exception {
        TypeCheck.addSubTypes(e, syntax.getSubtypes());
        name = e.getAttribute("name");
        ref = e.getAttribute("ref");        
        level = 0;
        try {
            level = Integer.parseInt(e.getAttribute("level"));
        } catch (NumberFormatException numberFormatException) {
        }
        changes = new HashMap<>();
        String replace = e.getAttribute("changes");
        if (!replace.isEmpty()) {
            String[] couple = replace.split("/");
            if (couple.length == 2) {
                changes.put(new Expression(couple[0], syntax), new Expression(couple[1], syntax));
            } else {
                throw new Exception("format de remplacement incorrect");
            }
        }
        result = e.getFirstChild().getTextContent().trim();
    }

    /**
     * ajoute une ExprNode à la liste exprNodes si elle est nouvelle et non de type
     * discards 
     *
     * @param en ExprNode à ajouter
     * @param vars valeurs des variables à remplacer
     * @param freevars
     * @param listvars
     * @param syntax
     * @param exprNodes liste déjà établie
     * @param exprDiscards
     * @return l'exprNode ou null si ne convient pas
     * @throws Exception
     */
    public ExprNode addExpr(ExprNode en, HashMap<Expression, Expression> vars, 
            HashMap<String,String> freevars, ArrayList<Expression> listvars, 
            Syntax syntax, ArrayList<ExprNode> exprNodes, ArrayList<ExprNode> exprDiscards) throws Exception {
        ExprNode ret = null;
        //* modif
        Expression e = applyVars(en, vars, syntax);
        //*/
        /* avant
        Expression e = new Expression(getResult(), syntax);
        e = e.replace(vars);
        if (!name.isEmpty()) {
            e.setType(name);
        }
        en.setE(e); // (A->B)->(A->((B->C)->((A->B)->(A->C)))) ne devrait pas passer, A->(B->T)
        //*/
        boolean inlist = false;
        for (ExprNode exprNode : exprNodes) {
            Expression expr = exprNode.getE();
            HashMap<Expression, Expression> nvars = new HashMap<>();
            if (e.matchRecursively(expr, freevars, listvars, nvars, syntax.getSubtypes(), en)) {
                // déjà dans la liste (aux variables près)
                if (!exprNode.getParentList().containsAll(en.getParentList())) {
                    exprNode.getParentList().addAll(en.getParentList());
                }
                inlist = true;
                break;
            }
        }
        if (!inlist) {
            if (exprDiscards != null) {
                for (ExprNode exprNode : exprDiscards) {
                    Expression expr = exprNode.getE();
                    HashMap<Expression, Expression> nvars = new HashMap<>();
                    if (e.matchRecursively(expr, freevars, listvars, nvars, syntax.getSubtypes(), en)) {
                        inlist = true;
                        break;
                    }
                }
            }
            if (!inlist) {
                exprNodes.add(en);
                ret = en;
                if (!en.getParentList().isEmpty()) {
                    for (int i = 0; i < en.getParentList().get(0).length; i++) {
                        int j = en.getParentList().get(0)[i];
                        ExprNode en1 = exprNodes.get(j);
                        if (!en1.getChildList().contains(exprNodes.size() - 1)) {
                            en1.getChildList().add(exprNodes.size() - 1);
                        }
                    }
                }
            }
        }
        return ret;
    }
    
  /**
   *
   * @param en expression à transformer
   * @param vars table des variables et remplacements
   * @param syntax
   * @return l'expression transformée
   * @throws java.lang.Exception
   */
  public Expression applyVars(ExprNode en, HashMap<Expression, Expression> vars, Syntax syntax) 
          throws Exception {
      Expression e = new Expression(getResult(), syntax);
        e = e.replace(vars);
        if (!name.isEmpty()) {
            e.setType(name);
        }
        en.setE(e);
        return e;
    }
  

    @Override
    public String toString() {
        String ret = "";
        if (!changes.isEmpty()) {
            for (Map.Entry<Expression, Expression> change : changes.entrySet()) {
                ret += "(" + change.getValue() + "/" + change.getKey() + ")";
            }
        }
        ret += getResult();
        return ret;
    }

    public String getResult() {
        return result;
    }

    public HashMap<Expression, Expression> getChanges() {
        return changes;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public int getLevel() {
        return level;
    }
}
