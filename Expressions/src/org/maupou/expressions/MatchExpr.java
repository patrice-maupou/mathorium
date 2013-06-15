/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 *
 * @author Patrice Maupou
 */
public class MatchExpr {

  private String regex;
  private String node;
  private String type;
  private Expression schema;
  private ArrayList<Expression> discards; // expr->type
  private HashMap<String, String> conds;  // donnée par une égalité
  private HashMap<String, String> searches; // type0->type1 remplacement possible de type t0 par t1
  private HashMap<Expression, String> vars; // var->type

  /**
   * Définition des paramètres à partir de l'élément
   *
   * @param match élément de référence
   * @param syntax
   * @throws Exception
   */
  public MatchExpr(Element match, Syntax syntax) throws Exception {
    conds = new HashMap<>();
    vars = new HashMap<>();
    node = match.getAttribute("node");
    type = match.getAttribute("name");
    Node txtNode = match.getFirstChild();
    if (txtNode != null) {
      regex = txtNode.getTextContent();
      schema = new Expression(regex, syntax);
    }
    else {
      String[] listconds = match.getAttribute("conds").split(",");
      for (int i = 0; i < listconds.length; i++) {
        if (!listconds[i].isEmpty()) {
          String[] cond = listconds[i].split("=");
          String key = cond[0];
          String value = (cond.length == 2) ? cond[1] : null;
          conds.put(key, value);
        }
      }
    }
    String searchtext = match.getAttribute("search");
    if (!searchtext.isEmpty()) {
      searches = new HashMap<>();
      String[] coupleTypes = searchtext.split(",");
      for (int i = 0; i < coupleTypes.length; i++) {
        String couple = coupleTypes[i];
        Pattern p = Pattern.compile("(?<t0>\\w+)/(?<t1>\\w+)");
        Matcher m = p.matcher(couple);
        if (m.matches()) {
          searches.put(m.group("t0"), m.group("t1"));
        }
      }
      extract(schema, vars);
    }
    String types = match.getAttribute("types");
    if (!types.isEmpty()) {
      String[] couples = types.split(",");
      for (int i = 0; i < couples.length; i++) {
        String[] typeOf = couples[i].split(":");
        Expression e = new Expression(typeOf[0], syntax);
        e.setType(typeOf[1]);
        schema.updateType(e);
      }
    }
    discards = new ArrayList<>();
    String discardtxt = match.getAttribute("discard");
    if (!discardtxt.isEmpty()) {
      String[] coupleDiscard = discardtxt.split(",");
      for (int i = 0; i < coupleDiscard.length; i++) {
        String couple = coupleDiscard[i];
        Pattern p = Pattern.compile("(?<e>\\w+):(?<t>\\w+)");
        Matcher m = p.matcher(couple);
        if (m.matches()) {
          Expression discard = new Expression(m.group("e"), syntax);
          discard.setType(m.group("t"));
          discards.add(discard);
        }
      }
    }
  }
  
  /**
   * extrait la variable éventuelle dans la table vars
   * @param schema
   * @param vars 
   */
    private void extract(Expression schema, HashMap<Expression, String> vars) {
        Expression e = schema.copy();
        if(searches.containsKey(e.getType())) {            
            vars.put(e, searches.get(e.getType()));
        }
        else if(e.getChildren() != null) {
            for (Expression child : e.getChildren()) {
                extract(child, vars);
            }
        }
    }

  /**
   * liste des expressions de la liste exprs qui conviennent
   *
   * @param vars
   * @param syntax
   * @param exprNodes
   * @return la liste des chaînes qui conviennent
   * @throws Exception
   */
  public ArrayList<String> searchExprs(HashMap<Expression, Expression> vars, Syntax syntax,
          ArrayList<ExprNode> exprNodes) throws Exception {
    ArrayList<String> nexprs = new ArrayList<>();
    HashMap<Expression, Expression> nvars = new HashMap<>();
    for (ExprNode en : exprNodes) {
      nvars.clear();
      for (Map.Entry<Expression, Expression> entry : vars.entrySet()) {
        nvars.put(entry.getKey().copy(), entry.getValue().copy());
      }
      if (checkExpr(en, nvars, syntax)) {
        nexprs.add(en.getE().toString(syntax.getSyntaxWrite()));
      }
    }
    vars.clear();
    return nexprs;
  }

  /**
   * vérifie si expr correspond au schema, et si oui, les nouvelles variables de même nom que celles
   * dans vars doivent aussi correspondre
   *
   * @param en l'expr examinée par rapport à schema
   * @param vars table des variables déjà connues, ex: A:=(A->B)->A
   * @param syntax
   * @return vrai si l'expression convient
   * @throws Exception
   */
  public boolean checkExpr(ExprNode en, HashMap<Expression, Expression> vars, Syntax syntax)
          throws Exception {
    boolean ret = false;
    Expression expr = en.getE().copy();
    if (schema == null) {
      ret = checkConditions(en, syntax);
    }
    else if (expr != null) { // ex : A->B type: prop
      HashMap<Expression, Expression> nvars = new HashMap<>();
      ret = expr.match(schema, searches, nvars, syntax.getSubtypes());
      if (vars.isEmpty()) {
        vars.putAll(nvars);
      }
      else { // actualiser nvars  A:=(A->B)->C  B:=B->C par rapport à vars
        HashMap<Expression, Expression> aux = new HashMap<>();
        for (Map.Entry<Expression, Expression> entry : vars.entrySet()) {
          Expression var = nvars.get(entry.getKey());
          Expression e = entry.getValue();
          if (ret && var != null) { // (A->B)->C  entry.getValue():=A->(B->A)
            ret &= e.match(var, searches, aux, syntax.getSubtypes());
          }
        }
        if (ret) {
          for (Map.Entry<Expression, Expression> entry : nvars.entrySet()) {
            int index = discards.indexOf(entry.getKey());
            if (index != -1) {
              ret = !discards.get(index).getType().equals(entry.getValue().getType());
            }
            vars.put(entry.getKey(), entry.getValue().replace(aux)); // actualisation
          }
        }
      }
    }
    return ret;
  }

  /**
   *
   * @param en
   * @param syntax
   * @return
   */
  public boolean checkConditions(ExprNode en, Syntax syntax) {
    boolean ret = false;
    for (Map.Entry<String, String> entry : conds.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      try {
        int max = Integer.parseInt(value);
          switch (key) {
              case "parents":
                  ret |= en.getParentList().size() <= max;
                  break;
              case "childs":
                  ret |= en.getChildList().size() <= max;
                  break;
              case "depth":
                  ret |= en.getE().depth() > max;
                  break;
          }
      } catch (NumberFormatException numberFormatException) {
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    String ret = "regex = " + regex + "   " + node + type + " " + conds;
    return ret;
  }

  public String getRegex() {
    return regex;
  }

  public String getNode() {
    return node;
  }

  public String getType() {
    return type;
  }

  public HashMap<String, String> getSearches() {
    return searches;
  }

    /**
     * @return the vars
     */
    public HashMap<Expression, String> getVars() {
        return vars;
    }

}
