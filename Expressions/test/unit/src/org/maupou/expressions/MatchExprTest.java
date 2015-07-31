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
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Patrice
 */
public class MatchExprTest {

  private Syntax syntax;
  private HashMap<String, String> typesMap;
  ArrayList<Expression> listvars;
  private String[] entries;
  private ArrayList<Schema> schemas;

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
    if (syntaxFile.isFile() && expressions.isFile()) {
      try {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(syntaxFile);
        syntax = new Syntax(document);
        syntax.addGenerators(document);
        Generator gen = syntax.getGenerators().get(0);
        ArrayList<GenItem> genItems = gen.getGenItems();
        typesMap = genItems.get(0).getTypesMap();
        listvars = genItems.get(0).getListvars();
        for (GenItem genItem : genItems) {
          if (genItem.toString().equals("modus ponens")) {
            schemas = genItem.getSchemas();
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
   * Test of checkExpr method, of class MatchExpr.
   *
   * @throws java.lang.Exception
   */
  @Test
  public void testCheckExpr() throws Exception {
    System.out.println("checkExpr");
    /*
    HashMap<Expression, Expression> vars = new HashMap<>(), varsExpected = new HashMap<>();
    ArrayList<Expression> es = new ArrayList<>();
    for (int i = 1; i < entries.length - 1; i++) {
      if (i < 3) {
        Expression e = new Expression(entries[i], syntax);
        e.setType("thm");
        es.add(e);
      } else {
        String[] v = entries[i].split("=");
        varsExpected.put(new Expression(v[0], syntax), new Expression(v[1], syntax));
      }
    }
    MatchExpr matchExpr = (MatchExpr) schemas.get(0);
    //*/
  }

  private static void printVars(HashMap<Expression, Expression> vars, Syntax syntax)
          throws Exception {
    System.out.println("vars = {");
    for (Map.Entry<Expression, Expression> entry : vars.entrySet()) {
      String v = entry.getKey().toString();
      String val = syntax.getSyntaxWrite().toString(entry.getValue());
      System.out.print(v + " = " + val + "\n");
    }
    System.out.print("}\n");
  }

  /**
   * Test of toString method, of class MatchExpr.
   */
  @Test
  @Ignore
  public void testToString() {
    System.out.println("toString");
    MatchExpr instance = null;
    String expResult = "";
        //String result = instance.toString();
    //assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
  
  @Test
  public void testvarsInExpression() throws Exception {
    System.out.println("varsInExpression");
    /*
    ArrayList<Expression> varsexpected = new ArrayList<>();
    int[] rgs = new int[] {0,1,3};
    for (int i = 0; i < 3; i++) {
      varsexpected.add(listvars.get(rgs[i]));
    }
    ArrayList<Expression> vars = new ArrayList<>();
    Expression e = new Expression("A->(B->D)", syntax);
    MatchExpr.varsInExpression(e, vars, listvars);    
    System.out.println(vars);
    assertEquals(varsexpected, vars);
    //*/
  }

  /**
   * Test of extendMap method, of class MatchExpr.
   */
  @Test
  @Ignore
  public void testExtendMap() {
    System.out.println("extendMap");
    Expression e = null;
    HashMap<Expression, Expression> aux = null;
    ArrayList<Expression> vars = null;
    MatchExpr.extendMap(e, aux, vars);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
