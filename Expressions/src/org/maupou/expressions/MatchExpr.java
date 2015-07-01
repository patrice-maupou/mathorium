/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Patrice Maupou
 */
public class MatchExpr {

  private final String type;
  private Expression schema, global;
  private final HashMap<Expression, Expression> replaceMap; // TODO : ArrayList<Result> à la place
  private final ArrayList<Expression> vars;
  private final boolean recursive, bidir;

  /**
   * Définition des paramètres à partir de l'élément
   *
   * @param match élément de référence
   * @param syntax
   * @param listvars
   * @throws Exception
   */
  public MatchExpr(Element match, Syntax syntax, ArrayList<Expression> listvars) throws Exception {
    HashMap<String, String> options = new HashMap<>();
    vars = new ArrayList<>();
    type = match.getAttribute("name");
    String s = match.getAttribute("global");
    if (!s.isEmpty()) {
      global = new Expression(s, type, null, true);
    }
    String[] listopts = match.getAttribute("options").split(",");
    for (String option : listopts) {
      if (!option.isEmpty()) {
        String[] pair = option.split("=");
        if (pair.length == 2) {
          options.put(pair[0], pair[1]);
        }
      }
    }
    recursive = "yes".equals(options.get("recursive"));
    bidir = "yes".equals(options.get("bidirectional"));

    // table de remplacement des expressions
    replaceMap = new HashMap<>();
    Node txtNode = match.getFirstChild();
    Expression key = null;
    int cnt = 0;
    while (txtNode != null) { // boucle des modèles
      if (txtNode.getNodeType() == Node.CDATA_SECTION_NODE) {
        if (key == null) {
          key = new Expression(txtNode.getTextContent(), syntax);
          replaceMap.put(key, null);
          if (cnt == 0) { // le premier élément est schema
            schema = key;
            varsInExpression(key, vars, listvars);
          }
        } else {
          Expression value = new Expression(txtNode.getTextContent(), syntax);
          key = replaceMap.put(key, value);
        }
        cnt++;
      }
      txtNode = txtNode.getNextSibling();
    }
    //
    String types = match.getAttribute("types");
    if (!types.isEmpty()) {
      String[] couples = types.split(",");
      for (String couple : couples) {
        String[] typeOf = couple.split(":");
        Expression e = new Expression(typeOf[0], syntax);
        e.setType(typeOf[1]);
        schema.updateType(e);
      }
    }
  }

  /**
   * match l'expression expr en tenant compte de la table vars (variables déjà attribuées). 
   * 1. si global = null, match direct de expr contre schema. 
   * 2. sinon, transformation de l'expression par la table replaceMap, résultat dans 
   * la variable global
   *
   * @param expr l'expression examinée par rapport à schema, ex : ((A->B)->C)->(B->C)
   * @param typesMap table de remplacement d'un type par un autre (propse->prop)
   * @param listvars liste des symboles à remplacer
   * @param vars table des variables déjà connues, ex: {A:=(A->B)->A}
   * @param syntax
   * @return true si l'expression convient
   * @throws Exception
   */
  public boolean checkExpr(Expression expr, HashMap<Expression, Expression> vars,
          HashMap<String, String> typesMap, ArrayList<Expression> listvars, Syntax syntax)
          throws Exception {
    boolean ret = true;
    if (expr != null) { // schema : A->B type: prop
      HashMap<Expression, Expression> svars = new HashMap<>(), evars = new HashMap<>();
      if (global == null) {
        if (bidir) {
          ret = expr.matchBoth(schema, typesMap, listvars, evars, svars, syntax.getSubtypes());
        } else {
          ret = expr.match(schema, typesMap, listvars, svars, syntax.getSubtypes());
        }
        if (vars.isEmpty()) { // ajouter les nouvelles variables à la table vars
          vars.putAll(svars);
          listvars.stream().forEach((var) -> {
            var.setSymbol(true);
          });
        } else { // ce n'est pas le premier modèle
          HashMap<Expression, Expression> nsvars = new HashMap<>(), nvars = new HashMap<>();
          for (Map.Entry<Expression, Expression> var : vars.entrySet()) {
            Expression svar = svars.get(var.getKey()); // A->B
            Expression e = var.getValue(); // (A->B)->C
            if (ret && svar != null) { // nsvars={A=(A->B)->C, B=B->C} mais pas le C de B:=  
              ret &= e.match(svar, typesMap, listvars, nsvars, syntax.getSubtypes());
            }
          }
          if (ret) {
            // renomme certaines variables
            svars.values().stream().forEach((e) -> {extendMap(e, nsvars, listvars);});
            // corrige vars avec svars
            vars.keySet().stream().forEach((var) -> {vars.put(var, vars.get(var).replace(nvars));});
            svars.keySet().stream().forEach((svar) -> {svars.put(svar, svars.get(svar).replace(nsvars));});
            vars.putAll(svars);
          }
        }
        expr.markUsedVars(listvars);
      } else { // il s'agit de remplacements
        Expression e = expr;
        boolean[] modifs = new boolean[1];
        do {
          modifs[0] = false;
          e = e.matchsubExpr(getReplaceMap(), modifs, typesMap, listvars, svars,
                  syntax.getSubtypes());
        } while (recursive && modifs[0]);
        vars.put(global, e);
      }
    } else { // expr est nulle : vérifier si le schéma correspond au type
      Expression e = schema.replace(vars);
      ret = type.equals(e.getType());
    }
    return ret;
  }
  
  /**
   * ajoute à la liste vars les variables de listvars qui composent l'expression e
   * @param e
   * @param vars
   * @param listvars 
   */
  public static void varsInExpression(Expression e, ArrayList<Expression> vars, ArrayList<Expression> listvars) {
    if (listvars.indexOf(e) == -1) {
      if(e.getChildren() != null) {
        e.getChildren().stream().forEach((child) -> {varsInExpression(child, vars, listvars);});
      }
    }
    else {
      vars.add(e);
    }
  }

  /**
   * ajoute à la table vars les variables de e déjà utilisées dans les valeurs de vars
   *
   * @param e expression
   * @param vars table variable=valeur
   * @param listvars liste de référence des variables
   */
  public static void extendMap(Expression e, HashMap<Expression, Expression> vars,
          ArrayList<Expression> listvars) {
    int index = listvars.indexOf(e);
    if (index != -1) {
      Expression evar = listvars.get(index);
      if (!evar.isSymbol() && !vars.containsKey(e)) {
        for (Expression var : listvars) {
          if (var.isSymbol() && var.getType().equals(e.getType())) {
            var.setSymbol(false);
            vars.put(e, var); // changement de variable
            break;
          }
        }
      }
    } else if (e.getChildren() != null) {
      e.getChildren().stream().forEach((child) -> {
        extendMap(child, vars, listvars);
      });
    }
  }

  @Override
  public String toString() {
    String ret = replaceMap.toString();
    return ret;
  }


  public Expression getGlobal() {
    return global;
  }

  public ArrayList<Expression> getVars() {
    return vars;
  }


  public HashMap<Expression, Expression> getReplaceMap() {
    return replaceMap;
  }

}
