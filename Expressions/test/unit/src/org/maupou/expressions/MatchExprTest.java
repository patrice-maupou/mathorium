/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.maupou.expressions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice
 */
public class MatchExprTest {
    
    private Syntax syntax;
    private ArrayList<MatchExpr> matchExprs;
    private HashMap<String, String> freevars;
    ArrayList<Expression> listvars;
    private String[] entries;
    
    
    
    public MatchExprTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        File directory = new File("C:/Users/Patrice/Documents/NetBeansProjects/MathFiles");
        File syntaxFile = new File(directory, "prop_syntax.syx");
        File expressions = new File(directory, "propTests.xml");
        if(syntaxFile.isFile() && expressions.isFile()) {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(syntaxFile);
                syntax = new Syntax(document);
                syntax.addGenerators(document);
                Generator gen = syntax.getGenerators().get(0);
                ArrayList<GenItem> genItems = gen.getGenItems();
                freevars = genItems.get(0).getFreevars();
                listvars = genItems.get(0).getListvars();
                for (GenItem genItem : genItems) {
                    if(genItem.getName().equals("modus ponens")) {
                        matchExprs = genItem.getMatchExprs();
                        break;
                    }
                }
                document = documentBuilder.parse(expressions);
                Element texts = document.getElementById("checkExpr");
                String checkExpr = texts.getTextContent();
                entries = checkExpr.split("\",\\s+\"");
            } catch (Exception ex) {
                Logger.getLogger(MatchExprTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of checkExprNode method, of class MatchExpr.
     * @throws java.lang.Exception
     */
    /*@Test
    public void testCheckExprNode() throws Exception {
        System.out.println("checkExprNode");
        ExprNode en = null;
        HashMap<Expression, Expression> vars = new HashMap<>();
        Syntax syntax = null;
        MatchExpr instance = null;
        boolean expResult = false;
        boolean result = matchExprs.get(0).checkExprNode(en, freevars, listvars, vars, syntax);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }//*/

    /**
     * Test of checkExpr method, of class MatchExpr.
     * @throws java.lang.Exception
     */
    @Test
    public void testCheckExpr() throws Exception {
        System.out.println("checkExpr");
        HashMap<Expression, Expression> vars = new HashMap<>(), varsExpected = new HashMap<>();
        ArrayList<Expression> es = new ArrayList<>();
        for (int i = 1; i < entries.length-1; i++) {
            if(i < 3) {
                Expression e = new Expression(entries[i], syntax);
                e.setType("thm");
                es.add(e);
            }
            else {
                String[] v = entries[i].split("=");
                varsExpected.put(new Expression(v[0], syntax), new Expression(v[1], syntax));
            }
        } 
        boolean result = matchExprs.get(0).checkExpr(es.get(0), vars, freevars, listvars, syntax);
        System.out.println(es.get(0).toString(syntax.getSyntaxWrite()));
        printVars(vars, syntax);
        if(result) {
            matchExprs.get(1).checkExpr(es.get(1), vars, freevars, listvars, syntax);
        }        
        System.out.println(es.get(1).toString(syntax.getSyntaxWrite()));
        printVars(vars, syntax);
        assertEquals(varsExpected, vars);
    }
    
    
    private static void printVars(HashMap<Expression, Expression> vars, Syntax syntax) 
            throws Exception {
        System.out.println("vars = {");
        for (Map.Entry<Expression, Expression> entry : vars.entrySet()) {
            String v = entry.getKey().toString();
            String val = entry.getValue().toString(syntax.getSyntaxWrite());
            System.out.print(v + " = " + val + "\n");
        }
        System.out.print("}\n");
    }


    /**
     * Test of toString method, of class MatchExpr.
     */
    /*/
    @Test
    public void testToString() {
        System.out.println("toString");
        MatchExpr instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }//*/


    /**
     * Test of extendMap method, of class MatchExpr.
     */
    
    @Test
    public void testExtendMap() {
        System.out.println("extendMap");
        Expression e = null;
        HashMap<Expression, Expression> aux = null;
        ArrayList<Expression> listvars = null;
        MatchExpr.extendMap(e, aux, listvars);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
