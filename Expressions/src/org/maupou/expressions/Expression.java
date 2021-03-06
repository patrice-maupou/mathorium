package org.maupou.expressions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Patrice Maupou
 */
public class Expression {

    private final String name;
    private String type;
    private ArrayList<Expression> children;
    private boolean symbol;
    private int richness;

    /**
     * Création directe en connaissant tous les paramètres
     *
     * @param name
     * @param type
     * @param children
     * @param symbol
     */
    public Expression(String name, String type, ArrayList<Expression> children, boolean symbol) {
        this.name = name;
        this.type = type;
        this.children = children;
        this.symbol = symbol;
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
        symbol = false;
        if (e != null) {
            name = e.getName();
            type = e.getType();
            children = e.getChildren();
        } else {
            name = "non valid Expression";
            type = null;
            throw new Exception("Expression <" + text + "> non valide");
        }
    }

    /**
     * inverse de toText()
     *
     * @param text texte complet avec les types
     * @throws Exception
     */
    public Expression(String text) throws Exception {
        symbol = false;
        ArrayList<Expression> list = new ArrayList<>();
        String[] ret = scanExpr(text, list); 
        if(list.size() == 1 && ret[1].isEmpty()) {
            name = list.get(0).getName();
            type = list.get(0).getType();
            children = list.get(0).getChildren();
        } else {
            name = "non valid Expression";
            type = null;
            throw new Exception("Expression <" + text + "> non valide");
        }
    }

    /**
     * la fin est basée sur les valeurs possibles du dernier match "simple;type"
     * si le marqueur est "):type" on ajoute l'expression à list, on retourne.
     * "," on ajoute l'expression à list, on continue
     *
     * @param text chaîne à analyser
     * @param list la liste des expressions scannées
     * @return le texte restant et le dernier marqueur
     */
    public static String[] scanExpr(String text, ArrayList<Expression> list) {
        String[] ret = new String[]{"", text};
        String mark = "";
        Matcher m = Pattern.compile("([^\\(\\),]+):(\\w+)(,|\\):|)").matcher(text);
        if (m.lookingAt()) { // expression simple
            String name = m.group(1);
            String type = m.group(2);
            mark = (m.groupCount() == 3) ? m.group(3) : "";
            list.add(new Expression(name, type, null, false));
            text = text.substring(m.end());
        } else { // composée
            m = Pattern.compile("\\((\\w+),").matcher(text); // exemple "(add,"
            if (m.lookingAt()) {
                String name = m.group(1);
                ArrayList<Expression> childs = new ArrayList<>();
                text = text.substring(m.end());
                ret = scanExpr(text, childs);
                text = ret[1];
                if (ret[0].equals("):") || ret[0].isEmpty()) { // fin normale
                    m = Pattern.compile("(\\w+)(,|\\):|)").matcher(text);
                    if (m.lookingAt()) {
                        mark = (m.groupCount() == 2)? m.group(2) : "";
                        String type = m.group(1);
                        Expression e = new Expression(name, type, childs, false);
                        list.add(e); // liste remplacée par un seul élément
                        text = text.substring(m.end());
                    }
                }
            }
        }
        if ("):".equals(mark) || mark.isEmpty()) { // niveau terminé
            ret = new String[]{mark, text};
        } else if (",".equals(mark)) { // même niveau
            ret = scanExpr(text, list);
        }
        return ret;
    }

    /**
     * Détermine les expressions simples, Exemple : 2+(3-4) donne "2"->2 ,
     * "+("->null , "3"->3, "-"->null , "4"->4 , ")"->null
     *
     * @param text
     * @param simpleRule
     * @return une table ordonnée où les parties de texte non interprétées ont
     * la valeur null
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
                    e = new Expression(matcher.group(), getType(), getChildren(), false);
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
     * @param tm la suite des sous-expressions déjà trouvées pos =
     * (longueur,expression)
     * @param syntax la grammaire
     * @param offset la position de la chaîne dans le texte complet
     */
    private Expression decreasingSearch(String text, String tokenvar, TreeMap<Integer, Expression> tm,
            List<SyntaxRule> listRules, HashMap<String, Set<String>> subtypes, int offset) {

        String tkvar = tokenvar.substring(0, 2) + "*";
        if (text.matches(tkvar)) { // expression décodée
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
                        if ((m.group(i) != null) && !(var && nodeName.equals(m.group(i)))) { // last child
                            e = decreasingSearch(m.group(i), tokenvar, tm, listRules, subtypes,
                                    offset + m.start(i));
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
                            e = new Expression(nodeName, typeCheck.getType(), ch, false);
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
            e = new Expression(name, type, null, isSymbol());
        } else {
            ArrayList<Expression> nchildren = new ArrayList<>();
            for (Expression children1 : children) {
                Expression child = children1.copy();
                nchildren.add(child);
            }
            e = new Expression(name, type, nchildren, isSymbol());
        }
        return e;
    }

    /**
     *
     * @param map associe une expression à une expression de remplacement
     * @return la nouvelle expression
     */
    public Expression replace(HashMap<Expression, Expression> map) {
        Expression e = map.get(this);
        if (e == null) {
            if (children != null) {
                ArrayList<Expression> echilds = new ArrayList<>();
                for (Expression children1 : children) {
                    echilds.add(children1.replace(map));
                }
                e = new Expression(name, type, echilds, isSymbol());
            } else {
                e = new Expression(name, type, null, isSymbol());
            }
        }
        return e;
    }

    /**
     * si l'expression contient une variable de listvars, on fixe le boolean
     * symbol de cette variable à la valeur false. (utilisé dans
     * MatchExpr.checkExpr)
     *
     * @param listvars liste de symboles, ceux déjà utilisés ne sont pas marqués
     */
    public void markUsedVars(ArrayList<Expression> listvars) {
        int index = listvars.indexOf(this);
        if (index != -1) { // c'est une variable
            listvars.get(index).setSymbol(false);
        } else if (children != null) {
            for (Expression child : children) {
                child.markUsedVars(listvars);
            }
        }
    }

    /**
     * si e est une sous-expression de l'expression actuelle, le etype est celui
     * de e
     *
     * @param e
     */
    public void updateType(Expression e) {
        if (this.equals(e)) {
            type = e.getType();
        } else if (children != null) {
            for (Expression children1 : children) {
                children1.updateType(e);
            }
        }
    }

    /**
     * examine les sous-expressions qui correspondent à schema et leur donne le
     * type de schema et les met dans la liste des Expr de en
     *
     * @param schema le modèle
     * @param freevars
     * @param listvars
     * @param en
     * @param vars la table des valeurs
     * @param subtypes
     * @return true si l'expression entière convient
     */
    public boolean matchRecursively(Expression schema,
            HashMap<String, String> freevars, ArrayList<Expression> listvars,
            HashMap<Expression, Expression> vars, HashMap<String, Set<String>> subtypes, ExprNode en) {
        boolean fit = match(schema, freevars, listvars, vars, subtypes);
        //*
        if (!fit) {
            for (Expression e : schema.getChildren()) {
                if (e.getChildren() != null) {
                    HashMap<Expression, Expression> nvars = new HashMap<>();
                    if (match(e, freevars, listvars, nvars, subtypes)) {
                        en.getExprs().add(schema.replace(nvars));
                        break;
                    }
                }
            }
        }
        //*/
        if (fit) {
            setType(schema.getType());
            en.getExprs().add(this);
        } else {
            if (children != null) {
                for (Expression child : children) {
                    HashMap<Expression, Expression> nvars = new HashMap<>();
                    child.matchRecursively(schema, freevars, listvars, nvars, subtypes, en);
                }
            }
        }
        return fit;
    }

    /**
     * vérifie si cette expression correspond à l'expression schema en
     * remplaçant les clés de map par des expressions de etype value ex:
     * A->(B->(A->B)) avec A->(B->C) avec C=A->B
     *
     * @param schema l'expression contenant les variables et servant de modèle
     * @param freevars table associant à un type de variable un type de
     * remplacement
     * @param listvars liste des variables susceptibles d'être utilisées
     * @param vars table des variables à affecter
     * @param subtypes
     * @return true l'expression est du modèle indiqué
     */
    public boolean match(Expression schema,
            HashMap<String, String> freevars, ArrayList<Expression> listvars,
            HashMap<Expression, Expression> vars, HashMap<String, Set<String>> subtypes) {
        boolean fit;
        Expression e;
        String vtype = (listvars.contains(schema)) ? freevars.get(schema.type) : null;
        if (vtype != null) { // le type de remplacement existe
            if (fit = subtypes.get(vtype).contains(type)) { // type sous-type de vType
                if ((e = vars.get(schema)) != null) { // déjà dans la table vars
                    fit = e.equals(this);
                } else { // nouvelle entrée dans vars
                    vars.put(schema.copy(), copy());
                }
            }
        } else if (fit = name.equals(schema.name)) { // l'égalité doit être stricte entre e et schema
            if (children != null && (fit = children.size() == schema.getChildren().size())) {
                for (int i = 0; i < children.size(); i++) {
                    fit &= children.get(i).match(schema.getChildren().get(i), freevars, listvars,
                            vars, subtypes);
                }
            }
        }
        return fit;
    }

    /**
     * Transforme l'expression en utilisant la table des remplacements
     * replaceMap pour les sous-expressions qui conviennent.
     *
     * @param replaceMap table des transformations à effectuer
     * @param modifs liste qui contient un seul boolean modifié à true si
     * l'expression a été modifiée.
     * @param typesMap
     * @param listvars
     * @param vars table des variables des entrées de replaceMap
     * @param subtypes
     * @return vrai si une sous-expression est conforme au modèle
     */
    public Expression matchsubExpr(HashMap<Expression, Expression> replaceMap, boolean[] modifs,
            HashMap<String, String> typesMap, ArrayList<Expression> listvars,
            HashMap<Expression, Expression> vars, HashMap<String, Set<String>> subtypes) {
        Expression e = copy();
        for (Map.Entry<Expression, Expression> entry : replaceMap.entrySet()) {
            if (e.match(entry.getKey(), typesMap, listvars, vars, subtypes)) {
                e = entry.getValue().replace(vars);
                modifs[0] = true;
                return e;
            } else {
                vars.clear();
            }
        }
        if (e.getChildren() != null) {
            for (int i = 0; i < e.getChildren().size(); i++) {
                Expression child = e.getChildren().get(i);
                vars.clear();
                e.getChildren().set(i, child.matchsubExpr(replaceMap, modifs, typesMap,
                        listvars, vars, subtypes));
            }
        }
        return e;
    }

    /**
     * extension de match l'expression transformée de schema n'est plus
     * forcément égale à this, mais est une transformée de this.
     *
     * @param schema modèle
     * @param typesMap type0 peut être remplacé par son image type1 ex:
     * propvar->prop
     * @param listvars
     * @param vars table de remplacement des variables de this
     * @param schvars table de remplacement des variables de schema
     * @param subtypes
     * @return
     */
    public boolean matchBoth(Expression schema,
            HashMap<String, String> typesMap, ArrayList<Expression> listvars,
            HashMap<Expression, Expression> vars, HashMap<Expression, Expression> schvars,
            HashMap<String, Set<String>> subtypes) {
        Expression e;
        boolean fit;
        String vtype = (listvars.contains(schema)) ? typesMap.get(schema.type) : null;
        if (vtype != null) {
            if (fit = subtypes.get(vtype).contains(type)) { // le etype correspond
                if ((e = schvars.get(schema)) != null) { // déjà dans la table schvars
                    fit = e.equals(this);
                } else { // nouvelle entrée dans schvars
                    schvars.put(schema.copy(), copy());
                    for (Expression value : schvars.values()) {
                        value = value.replace(vars);
                    }
                }
            }
        } else if (listvars.contains(this)) { // l'expression est une variable
            vtype = typesMap.get(type);
            if (fit = vtype != null && subtypes.get(vtype).contains(schema.getType())) {
                if ((e = vars.get(this)) != null) {
                    fit = e.equals(schema);
                } else {
                    vars.put(copy(), schema.copy());
                }
                for (Expression value : vars.values()) {
                    value = value.replace(schvars);
                }
            }
        } else if (fit = name.equals(schema.name)) {
            if (fit = children.size() == schema.getChildren().size()) {
                for (int i = 0; i < children.size(); i++) {
                    Expression ei = children.get(i), si = schema.getChildren().get(i);
                    fit = ei.matchBoth(si, typesMap, listvars, vars, schvars, subtypes);
                }
            }
        }
        return fit;
    }

    /**
     * pas d'égalité de types requis, seulement sur les noeuds de l'arbre
     *
     * @param obj
     * @return
     */
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
                } else {
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
        if (children != null) {
            int nd = d;
            for (Expression child : children) {
                int dc = d + child.depth();
                if (nd < dc) {
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

    /**
     * Ecriture complète de l'expression comprenant le type et permettant de
     * reconstruire l'expression exemple : (ADD,3:natural,x:real):real
     *
     * @return la chaîne représentant l'expression
     */
    public String toText() {
        StringBuilder sb = new StringBuilder(getName());
        if (getChildren() != null) {
            sb.insert(0, "(");
            for (Expression child : getChildren()) {
                sb.append(",");
                sb.append(child.toText());
            }
            sb.append(")");
        }
        sb.append(":").append(type);
        return sb.toString();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Expression> getChildren() {
        return children;
    }

    public boolean isSymbol() {
        return symbol;
    }

    public void setSymbol(boolean symbol) {
        this.symbol = symbol;
    }

    public int getRichness() {
        return richness;
    }

}
