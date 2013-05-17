package org.maupou.expressions;

import java.util.*;
import java.util.regex.Matcher;

/**
 *
 * @author Patrice Maupou
 */
public class Expression {

  private String name;
  private String type;
  private ArrayList<Expression> children;
  private boolean valid;

  /**
   * Création directe en connaissant tous les paramètres
   *
   * @param name
   * @param type
   * @param children
   * @param valid
   */
  public Expression(String name, String type, ArrayList<Expression> children, boolean valid) {
    this.name = name;
    this.type = type;
    this.children = children;
    this.valid = valid;
  }

  /**
   * Construction par étapes d'une expression
   *
   * @param text
   * @param syntax
   * @throws Exception si l'écriture est non valide
   */
  public Expression(String text, Syntax syntax) throws Exception {
    Expression e = splitSimpleExpressions(text, syntax);
    if (e != null) {
      name = e.getName();
      type = e.getType();
      children = e.getChildren();
      valid = true;
    }
    else {
      name = "non valid Expression";
      valid = false;
      type = null;
      throw new Exception("Expression <" + text + "> non valide");
    }
  }

  /**
   * Détermine les expressions simples, Exemple : 2+(3-4) donne "2"->2 , "+("->null , "3"->3,
   * "-"->null , "4"->4 , ")"->null
   *
   * @param text
   * @param simpleRule
   * @return une table ordonnée où les parties de texte non interprétées ont la valeur null
   */
  private Expression splitSimpleExpressions(String text, Syntax syntax) {
    Expression e = null;
    boolean done = false;
    SyntaxRule simpleRule = syntax.getAtoms();
    String unused = syntax.getUnused();
    String tokenvar = "____________________________________";
    StringBuilder buf = new StringBuilder();
    while (buf.length() < text.length()) {
      buf.append(tokenvar);
    }
    buf.insert(0, unused);
    tokenvar = buf.toString();
    TreeMap<Integer, Expression> tm = new TreeMap<>();
    Matcher matcher = simpleRule.getPatternRule().matcher(text);
    while (matcher.find()) {
      for (int i = 0; i < matcher.groupCount(); i++) {
        if (matcher.group(i + 1) != null) {
          SyntaxPattern syntaxPattern = simpleRule.getSyntaxPatternGroups().get(i + 1);
          setType(syntaxPattern.getTypeChecks().get(0).getType());
          done = text.equals(matcher.group());
          e = new Expression(matcher.group(), getType(), getChildren(), true);
          tm.put(matcher.start(), e);
          text = text.substring(0, matcher.start())
                  + tokenvar.substring(0, matcher.end() - matcher.start())
                  + text.substring(matcher.end());
          break;
        }
      }
    }
    if (!done) {
      e = decreasingSearch(text, tokenvar, tm, syntax.getRules(), syntax.getSubtypes(), 0);
    }
    return e;
  }

  /**
   *
   * @param text la portion de texte à interpréter
   * @param tm la suite des sous-expressions déjà trouvées pos = (longueur,expression)
   * @param syntax la grammaire
   * @param offset la position de la chaîne dans le texte complet
   */
  private Expression decreasingSearch(String text, String tokenvar, TreeMap<Integer, Expression> tm,
          List<SyntaxRule> listRules, HashMap<String, Set<String>> subtypes, int offset) {

    String tkvar = tokenvar.substring(0, 2) + "*";
    if (text.matches(tkvar)) {
      return tm.get(offset);
    }
    Expression e = null;
    for (SyntaxRule rule : listRules) {
      String[] childs = rule.getChilds();
      Matcher m = rule.getPatternRule().matcher(text);
      while (m.find()) {
        // recherche du groupe correct
        Integer grouprange = -1, groupnext;
        for (Integer key : rule.getSyntaxPatternGroups().keySet()) {
          if (m.group(key) != null) {
            grouprange = key + 1;
            break;
          }
        }
        if (grouprange != -1) { // groupe trouvé
          SyntaxPattern syntaxPattern = rule.getSyntaxPatternGroups().get(grouprange - 1);
          String nodeName = syntaxPattern.getName();
          boolean var = true; // recherche de la variable comme groupe de capture
          try {
            nodeName = m.group(nodeName);
          } catch (IllegalArgumentException iea) {
            var = false;
          }
          groupnext = rule.getSyntaxPatternGroups().higherKey(grouprange);
          if (groupnext == null) {
            groupnext = m.groupCount() + 1;
          }
          ArrayList<Expression> ch = new ArrayList<>();
          for (int i = grouprange; i < groupnext; i++) {
            if ((m.group(i) != null) && !(var && nodeName.equals(m.group(i)))) { // next child
              e = decreasingSearch(m.group(i), tokenvar, tm, listRules, subtypes, offset + m.start(i));
              if (e == null) {
                return e;
              }
              if (ch.size() < childs.length) {
                ch.add(e);
                if (ch.size() == childs.length) {
                  break;
                }
              }
            }
          }
          // construction de l'expression
          boolean found = false;
          for (TypeCheck typeCheck : syntaxPattern.getTypeChecks()) {
            for (int i = 0; i < childs.length; i++) {
              String typeExpected = typeCheck.getChildtypes().get(childs[i]);
              if (!(found = subtypes.get(typeExpected).contains(ch.get(i).getType()))) {
                break;
              }
            }
            // élimination des descendants, changement dans text
            if (found) {
              e = new Expression(nodeName, typeCheck.getType(), ch, true);
              int start = m.start() + offset;
              for (int k = 1; k <= childs.length; k++) {
                start = tm.ceilingKey(start);
                Expression se = tm.get(start);
                if (nodeName.equals(childs[k - 1])) { // effacement au profit du descendant
                  e = se;
                }
                tm.remove(start);
              }
              tm.put(m.start() + offset, e);
              text = text.substring(0, m.start()) + tokenvar.substring(0, m.end() - m.start())
                      + text.substring(m.end());
              m.reset(text);
              if (text.matches(tkvar)) {
                return e;
              }
              break;
            }
          } // end liste typeChecks
        }
      } // end loop m.find()
    } // end loop rules
    if (!text.matches(tkvar)) {
      e = null;
    }
    return e;
  }

  /**
   * copie complète de l'expression
   *
   * @return
   */
  public Expression copy() {
    Expression e;
    if (children == null) {
      e = new Expression(name, type, null, valid);
    }
    else {
      ArrayList<Expression> nchildren = new ArrayList<>();
      for (int i = 0; i < children.size(); i++) {
        Expression child = children.get(i).copy();
        nchildren.add(child);
      }
      e = new Expression(name, type, nchildren, valid);
    }
    return e;
  }

  /**
   *
   * @param map
   * @return la nouvelle expression
   */
  public Expression replace(HashMap<Expression, Expression> map) {
    Expression e = map.get(this);
    if (e == null) {
      if (children != null) {
        ArrayList<Expression> echilds = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
          echilds.add(children.get(i).replace(map));
        }
        e = new Expression(name, type, echilds, valid);
      }
      else {
        e = new Expression(name, type, null, valid);
      }
    }
    return e;
  }

  /**
   * si e est une sous-expression de l'expression actuelle, le type est celui de e
   * @param e
   */
  public void updateType(Expression e) {
    if(this.equals(e)) {
      type = e.getType();
    }
    else if(children != null){
      for (int i = 0; i < children.size(); i++) {
        children.get(i).updateType(e);
      }
    }
  }

  /**
   * vérifie si cette expression correspond à l'expression schema en remplaçant
   * les variables de type0 par des expressions de type1
   * ex: A->(B->A)
   *
   * @param schema l'expression modèle ex: (A->B)->C pour
   * @param map type0 de schema peut être remplacé par son image type1 ex: propvar->prop
   * @param vars table des remplacements pour passer de schema à cette expression
   * @param subtypes table des sous-types
   * @return true si c'est possible
   */
  public boolean match(Expression schema, HashMap<String, String> map,
          HashMap<Expression, Expression> vars, HashMap<String, Set<String>> subtypes) {
    Expression e;
    boolean fit;
    String ntype = map.get(schema.getType()); // ex : (A->B)->C
    if (ntype != null) {  // remplacement possible, ex : schema:propvar ntype=prop
      if (fit = subtypes.get(ntype).contains(type)) { // le type correspond ntype <- type
        if ((e = vars.get(schema)) != null) { // déjà dans la table vars
          fit = e.equals(this);
        }
        else { // nouvelle entrée dans vars
          vars.put(schema.copy(), copy());
        }
      }
      else {
        fit = false;
      }
    }
    else if (fit = name.equals(schema.name)) {
      if (children != null && (fit = children.size() == schema.getChildren().size())) {
        for (int i = 0; i < children.size(); i++) {
          fit &= children.get(i).match(schema.getChildren().get(i), map, vars, subtypes);
        }
      }
    }
    return fit;
  }

  /**
   * extension de match l'expression transformée de schema n'est plus forcément égale à this,
   * mais est une transformée de this.
   *
   * @param schema
   * @param map autorise le changement d'un type par son image
   * @param replace table de remplacement des variables de this
   * @param sreplace table de remplacement des variables de schema
   * @param subtypes
   * @return
   */
  public boolean matchBoth(Expression schema, HashMap<String, String> map,
          HashMap<Expression, Expression> replace, HashMap<Expression, Expression> sreplace,
          HashMap<String, Set<String>> subtypes) {
    Expression e;
    boolean fit = schema.equals(this);
    if (!fit) {
      String ntype = map.get(schema.getType());
      if (ntype != null) {  // remplacement possible car variable de schema
        if (fit = subtypes.get(ntype).contains(type)) { // le type correspond
          if ((e = sreplace.get(schema)) != null) { // déjà dans la table sreplace
            fit = e.equals(this);
          }
          else { // nouvelle entrée dans sreplace
            sreplace.put(schema.copy(), copy());
            for (Expression value : sreplace.values()) {
              value = value.replace(replace);
            }
          }
        }
        else {
          fit = false;
        }
      }
      else if (map.get(type) != null) { // variable de this
        if (fit = subtypes.get(map.get(type)).contains(schema.getType())) {
          if ((e = replace.get(this)) != null) {
            fit = e.equals(schema);
          }
          else {
            replace.put(copy(), schema.copy());
          }
          for (Expression value : replace.values()) {
            value = value.replace(sreplace);
          }
        }
      }
      else if (fit = name.equals(schema.name) && subtypes.get(type).contains(schema.getType())) {
        if (fit = children.size() == schema.getChildren().size()) {
          for (int i = 0; i < children.size(); i++) {
            fit = children.get(i).matchBoth(schema.getChildren().get(i), map, replace, sreplace, subtypes);
          }
        }
      }
    }
    return fit;
  }

  @Override
  public boolean equals(Object obj) {
    boolean ret = false;
    if (obj != null && obj instanceof Expression) {
      Expression eobj = (Expression) obj;
      if (ret = name.equals(eobj.getName())) {
        if (children != null && eobj.getChildren() != null && children.size() == eobj.getChildren().size()) {
          for (int i = 0; i < children.size(); i++) {
            if (!(ret = children.get(i).equals(eobj.getChildren().get(i)))) {
              break;
            }
          }
        }
        else {
          ret = children == null && eobj.getChildren() == null;
        }
      }
    }
    return ret;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash + Objects.hashCode(this.name);
    hash = 89 * hash + Objects.hashCode(this.children);
    return hash;
  }

  public int depth() {
    int d = 1;
    if(children != null) {
      int nd = d;
      for (Expression child : children) {
        int dc = d + child.depth();
        if(nd < dc) {
          nd = dc;
        }
      }
      d = nd;
    }
    return d;
  }

  /**
   * écriture d'une expression en utilisant un fichier de syntaxe
   *
   * @param syntaxWrite donne la syntaxe
   * @return la chaîne
   * @throws Exception pas de fichier valide
   */
  public String toString(SyntaxWrite syntaxWrite) throws Exception {
    String ret = toString();
    if (children != null) {
      String unused = syntaxWrite.getUnused();
      NodeWrite node;
      for (int i = 0; i < syntaxWrite.getNodeWrites().size(); i++) {
        node = syntaxWrite.getNodeWrites().get(i);
        String nodename = node.getName();
        if (name.matches(nodename)) {
          ret = node.getValue();
          if (!node.getVar().isEmpty()) {
            ret = ret.replace(node.getVar(), name);
          }
          TreeMap<Integer, ChildReplace> childmap = node.getMapreplace();
          for (Map.Entry<Integer, ChildReplace> entry : childmap.entrySet()) {
            Integer j = entry.getKey();
            ChildReplace childReplace = entry.getValue();
            String childname = childReplace.getName();
            String replace = childReplace.getReplacement();
            List<String> conditions = childReplace.getConditions();
            Expression e = children.get(j);
            String ewr = e.toString(syntaxWrite);
            replace = (conditions.contains(e.getName())) ? replace.replace(childname, ewr) : ewr;
            int pos = ret.indexOf(unused);
            ret = ret.substring(0, pos) + replace + ret.substring(pos + unused.length());
          }
          break;
        }
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getName());
    if (getChildren() != null) {
      sb.insert(0, "(");
      for (Expression expression : getChildren()) {
        sb.append(",");
        sb.append(expression);
      }
      sb.append(")");
    }
    return sb.toString();
  }

  /**
   * le type de l'expression
   *
   * @return le type
   */
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  /**
   * liste des descendants de l'expression
   *
   * @return la liste
   */
  public ArrayList<Expression> getChildren() {
    return children;
  }

  /**
   * indique si l'expression est correctement écrite
   *
   *
   * @return true si c'est le cas
   */
  public boolean isValid() {
    return valid;
  }
}
