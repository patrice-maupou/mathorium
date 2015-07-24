package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice Maupou
 */
public class Result extends Schema {

  private final HashMap<Expression, Expression> changes;
  private final int level;

  /**
   *
   * @param result élément de document définissant l'instance pattern
   * @param sw
   * @throws Exception
   */
  public Result (Element result, SyntaxWrite sw) throws Exception {
    allowsChildren = false;
    int l = 0;
    try {
      if (result.hasAttribute("level")) {
        l = Integer.parseInt(result.getAttribute("level"));
      }
    } catch (NumberFormatException nfe) {
      throw new Exception("level incorrect : " + nfe.getMessage());
    }
    level = l;
    changes = new HashMap<>();
    String replace = result.getAttribute("changes");
    if (!replace.isEmpty()) {
      String[] couple = replace.split("/");
      if (couple.length == 2) {
        changes.put(new Expression(couple[0]), new Expression(couple[1]));
      } else {
        throw new Exception("format de remplacement incorrect");
      }
    }
    setPattern(result);
    setUserObject("résultat : " + getPattern().toString(sw));
  }

  /**
   * teste si une ExprNode est nouvelle et non de type discards
   *
   * @param en ExprNode à ajouter
   * @param vars valeurs des variables à remplacer
   * @param freevars
   * @param listvars
   * @param syntax
   * @param exprNodes liste déjà établie
   * @param exprDiscards
   * @return l'exprNode ou null si ne convient pas
   */
  public ExprNode newExpr(ExprNode en, HashMap<Expression, Expression> vars,
          HashMap<String, String> freevars, ArrayList<Expression> listvars,
          Syntax syntax, ArrayList<ExprNode> exprNodes, ArrayList<ExprNode> exprDiscards)  {
    ExprNode ret = null;
    Expression e = getPattern().copy().replace(vars);
    en.setE(e);
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
        break;  // (return null)
      }
    }
    if (!inlist) {
    //* inutile ?
      if (exprDiscards != null) {
        for (ExprNode exprNode : exprDiscards) {
          Expression expr = exprNode.getE();
          HashMap<Expression, Expression> nvars = new HashMap<>();
          if (e.matchRecursively(expr, freevars, listvars, nvars, syntax.getSubtypes(), en)) {
            inlist = true;
            break;
          }
        }
      } //*/
      if (!inlist) {
        ret = en;
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    String ret = "";
    if (!changes.isEmpty()) {
      ret = changes.entrySet().stream().map((change)
              -> "(" + change.getValue() + "/" + change.getKey() + ")").reduce(ret, String::concat);
    }
    ret += getPattern().toString();
    return ret;
  }

  /**
   * table de changements d'expressions (non utilisé)
   *
   * @return
   */
  public HashMap<Expression, Expression> getChanges() {
    return changes;
  }


  public int getLevel() {
    return level;
  }
}
