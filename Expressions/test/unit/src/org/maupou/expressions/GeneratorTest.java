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
    HashMap<Expression, Expression> replaceMap = new HashMap<>();
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
      replaceMap.clear();
      String[] replace = ms[i + 1].split(separator);
      for (int j = 0; j < replace.length; j += 2) {
        Expression key = new Expression(replace[j], syntax);
        Expression value = new Expression(replace[j + 1], syntax);
        replaceMap.put(key, value);
      }      
      Expression expected = new Expression(ms[i + 4], syntax);
      vars.clear();
      Expression result = gen.matchSubExpr(e, replaceMap);
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

  
  

  
  
}