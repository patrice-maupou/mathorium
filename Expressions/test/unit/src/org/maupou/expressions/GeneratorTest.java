/*
 * Copyright (C) 2015 Patrice.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.maupou.expressions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice
 */
@RunWith(Parameterized.class)
public class GeneratorTest { 
  
  private Syntax syntax;
  private SyntaxWrite syntaxWrite;
  private HashMap<String, Set<String>> subtypes;
  private String matches, matchBoth, matchsubExpr;
  private final String syxfile, tstfile;
  private static File sysDir, tstDir;
  /**
   * nom des fichiers à tester {syntaxe, exemples}
   * @return la liste des noms de fichiers
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Object[][] files = new Object[][]{
      {"number_syntax.syx", "generatorTests.xml"}
    };
    return Arrays.asList(files);
  }
  
  public GeneratorTest(String s, String t) {
    syxfile = s;
    tstfile = t;
    matches = "";
    matchBoth = "";
    matchsubExpr = "";
  }
  
  @BeforeClass
  public static void setUpClass() {
    sysDir = new File("C:/Users/Patrice/Documents/NetBeansProjects/Mathorium/Examples");
    tstDir = new File("C:/Users/Patrice/Documents/NetBeansProjects/Mathorium/datatest");
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
    File syntaxFile = new File(sysDir, syxfile);
    File expressions = new File(tstDir, tstfile);
    if (!syntaxFile.isFile() && expressions.exists()) {
      System.out.println("pas de fichier syntaxe : " + syntaxFile);
      return;
    }
    if (!expressions.exists()) {
      System.out.println("pas de fichier test : " + expressions);
    }
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(syntaxFile);
      syntax = new Syntax(document);
      syntaxWrite = syntax.getSyntaxWrite();
      subtypes = syntax.getSubtypes();
      document = documentBuilder.parse(expressions);
      Element doc = document.getElementById(syxfile);
      NodeList texts = doc.getElementsByTagName("texts");
      for (int i = 0; i < texts.getLength(); i++) {
        Element text = (Element) texts.item(i);
        if("matches".equals(text.getAttribute("name"))) {
          matches += text.getTextContent();
        }        
        if("matchBoth".equals(text.getAttribute("name"))) {
          matchBoth += text.getTextContent();
        }
        if("matchsubExpr".equals(text.getAttribute("name"))) {
          matchsubExpr = text.getAttribute("separator");
          matchsubExpr += text.getTextContent();
        }
      }
    } catch (Exception ex) {
      System.out.println("Pas de document");
    }
  }
  
  
  @After
  public void tearDown() {
  }

 /**
   * Test of match method, of class Generator
   * @throws java.lang.Exception
   */
  @Test
  public void testMatch() throws Exception {
    System.out.println("match");
    HashMap<Expression, Expression> vars = new HashMap<>();
    ArrayList<Expression> listvars = new ArrayList<>();
    String[] ms = matches.split("\",\\s+\"");
    String[] ls = ms[1].split("\\s"); // liste des variables
    for (String l : ls) {
      Expression v = new Expression(l, syntax);
      listvars.add(v);
    }
    Generator gen = new Generator(listvars, subtypes);
    for (int i = 2; i < ms.length - 1; i += 4) {
      Expression e = new Expression(ms[i], syntax);
      Expression s = new Expression(ms[i + 1], syntax);
      vars.clear();
      boolean result = gen.match(e, s, vars);
      System.out.println("" + vars);
      assertEquals(true, result);
    }
    System.out.println("");
  }

  /**
   * Test of matchBoth method, of class Generator.
   * @throws java.lang.Exception
   */
  @Test
  public void testMatchBoth() throws Exception {
    System.out.println("matchBoth");
    ArrayList<Expression> listvars = new ArrayList<>();
    HashMap<Expression, Expression> evars = new HashMap<>(), svars = new HashMap<>();
    String[] ms = matchBoth.split("\",\\s+\"");
    String[] ls = ms[1].split("\\s");
    for (String l : ls) {
      Expression v = new Expression(l, syntax);
      listvars.add(v);
    }
    Generator gen = new Generator(listvars, subtypes);
    for (int i = 2; i < ms.length - 1; i += 4) {
      Expression e = new Expression(ms[i], syntax);
      Expression s = new Expression(ms[i + 1], syntax);
      evars.clear();
      svars.clear();
      boolean fit = gen.matchBoth(e, s, evars, svars);
      System.out.println("" + evars);
      System.out.println("" + svars);
      System.out.println("e : " + syntaxWrite.toString(e) + "\ts : " + syntaxWrite.toString(s) + "\n");
      assertEquals(true, fit);
    }
  }

  /**
   * Test of matchSubExpr method, of class Generator.
   * @throws java.lang.Exception
   */
  @Test
  public void testMatchSubExpr() throws Exception {
    System.out.println("matchSubExpr");
    if (matchsubExpr == null) {
      fail("pas de données !");
      return;
    }
    ArrayList<Expression> listvars = new ArrayList<>();
    HashMap<Expression, Expression> vars = new HashMap<>();
    String[] ms = matchsubExpr.split("\",\\s+\"");
    String separator = ms[0];
    String[] ls = ms[1].split("\\s");
    for (String l : ls) {
      Expression v = new Expression(l, syntax);
      listvars.add(v);
    }
    Generator gen = new Generator(listvars, subtypes);
    for (int i = 2; i < ms.length - 1; i += 5) {
      Expression e = new Expression(ms[i], syntax);
      String[] replace = ms[i + 1].split(separator);
      Expression m = new Expression(replace[0], syntax);
      Expression r = new Expression(replace[1], syntax);
      Expression expected = new Expression(ms[i + 4], syntax);
      if (e.equals(expected)) {
        expected = null;
      }
      vars.clear();
      Expression result = gen.matchSubExpr(e, m, r);
      System.out.println(syntaxWrite.toString(e) + "  ->  " + syntaxWrite.toString(result));
      assertEquals(expected, result);
    }
    System.out.println("");
  }
 
   
  /**
   * Test of markUsedVars method, of class Generator.
   */
  @Test
  @Ignore
  public void testMarkUsedVars() {
    System.out.println("markUsedVars");
    Expression e = null;
    Generator instance = null;
    instance.markUsedVars(e);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class Generator.
   */
  @Test
  @Ignore
  public void testToString() {
    System.out.println("toString");
    Generator instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of nextmatch method, of class Generator.
   */
  @Ignore
  @Test
  public void testNextmatch() {
    System.out.println("nextmatch");
    Expression expr = null;
    MatchExpr matchExpr = null;
    Generator instance = null;
    boolean expResult = false;
    boolean result = instance.nextmatch(expr, matchExpr);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  

  /**
   * Test of newExpr method, of class Generator.
   */
  @Ignore
  @Test
  public void testNewExpr() {
    System.out.println("newExpr");
    ExprNode en = null;
    Result result_2 = null;
    ArrayList<ExprNode> exprNodes = null;
    Generator instance = null;
    boolean expResult = false;
    boolean result = instance.newExpr(en, result_2, exprNodes);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of matchRecursively method, of class Generator.
   */
  @Ignore
  @Test
  public void testMatchRecursively() {
    System.out.println("matchRecursively");
    Expression e = null;
    Expression s = null;
    HashMap<Expression, Expression> vars = null;
    Generator instance = null;
    boolean expResult = false;
    boolean result = instance.matchRecursively(e, s, vars);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of varsInExpression method, of class Generator.
   */
  @Ignore
  @Test
  public void testVarsInExpression() {
    System.out.println("varsInExpression");
    Expression e = null;
    ArrayList<Expression> vars = null;
    Generator instance = null;
    instance.varsInExpression(e, vars);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of extendMap method, of class Generator.
   */
  @Ignore
  @Test
  public void testExtendMap() {
    System.out.println("extendMap");
    Expression e = null;
    HashMap<Expression, Expression> vars = null;
    Generator instance = null;
    instance.extendMap(e, vars);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of setSchema method, of class Generator.
   */
  @Ignore
  @Test
  public void testSetSchema() {
    System.out.println("setSchema");
    Schema schema = null;
    Syntax syntax = null;
    Generator instance = null;
    instance.setSchema(schema, syntax);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getName method, of class Generator.
   */
  @Ignore
  @Test
  public void testGetName() {
    System.out.println("getName");
    Generator instance = null;
    String expResult = "";
    String result = instance.getName();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of log method, of class Generator.
   */
  @Ignore
  @Test
  public void testLog() {
    System.out.println("log");
    Generator instance = null;
    String expResult = "";
    String result = instance.log();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  
  

  
  
}
