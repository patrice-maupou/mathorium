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

  private String result;
  private HashMap<Expression, Expression> changes;
  private String name;
  private String ref;
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
    try {
      level = Integer.parseInt(e.getAttribute("level"));
    } catch (NumberFormatException numberFormatException) {
      level = 0;
    }
    changes = new HashMap<>();
    String replace = e.getAttribute("changes");
    if (!replace.isEmpty()) {
      String[] couple = replace.split("/");
      if (couple.length == 2) {
        changes.put(new Expression(couple[0], syntax), new Expression(couple[1], syntax));
      }
      else {
        throw new Exception("format de remplacement incorrect");
      }
    }
    result = e.getFirstChild().getTextContent().trim();
  }

  /**
   *
   * @param en
   * @param vars
   * @param syntax
   * @param exprNodes
   * @return
   * @throws Exception
   */
  public ExprNode addExpr(ExprNode en, HashMap<Expression, Expression> vars, Syntax syntax,
          ArrayList<ExprNode> exprNodes) throws Exception {
    ExprNode ret = null;
    Expression e = new Expression(result, syntax);
    e = e.replace(vars);
    e = e.replace(changes);
    String type = name;
    if ("inherit".equals(type)) {
      Expression er = new Expression(result, syntax);
      ExprNode res = new ExprNode(er, null, null, exprNodes);
      int n = exprNodes.indexOf(res);
      if (n != -1) {
        type = exprNodes.get(n).getE().getType();
      }
    }
    if (!type.isEmpty()) {
      e.setType(type);
    }
    en.setE(e);
    int index = exprNodes.indexOf(en);
    if (index != -1 && "inherit".equals(e.getType())) {
      e.setType(exprNodes.get(index).getE().getType());
    }
    boolean no = false;
    /* reporter
    for (int i = 0; i < syntax.getDiscards().size(); i++) {
      MatchExpr discard = syntax.getDiscards().get(i);
      if (no = discard.checkExpr(en, new HashMap<Expression, Expression>(), syntax)) {
        break;
      }
    }
    //*/
    if ((index = exprNodes.indexOf(en)) != -1) { // mise à jour si en déjà dans la liste
      ExprNode en1 = exprNodes.get(index);
      if (!en1.getParentList().containsAll(en.getParentList())) {
        en1.getParentList().addAll(en.getParentList());
      }
    }
    else if (!no) { // nouveau résultat
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
    return ret;
  }

  @Override
  public String toString() {
    String ret = "";
    if (!changes.isEmpty()) {
      for (Map.Entry<Expression, Expression> change : changes.entrySet()) {
        ret += "(" + change.getValue() + "/" + change.getKey() + ")";
      }
    }
    ret += result;
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
