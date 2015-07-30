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

/**
 *
 * @author Patrice
 */
 @RunWith(Parameterized.class)
public class GenItemTest {
  
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
      {"number_syntax.syx", "genItemTests.xml"}
    };
    return Arrays.asList(files);
  }
  
  /**
   *
   * @param s
   * @param t
   */
  public GenItemTest(String s, String t) {
    syxfile = s;
    tstfile = t;
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
      Element texts = document.getElementById("matches");
      if (texts != null) {
        matches = texts.getTextContent();
      }
      texts = document.getElementById("matchsubExpr");
      if (texts != null) {
        matchsubExpr = texts.getTextContent();
        String separator = texts.getAttribute("separator");
        matchsubExpr = separator + matchsubExpr;
      }
      texts = document.getElementById("matchBoth");
      if (texts != null) {
        matchBoth = texts.getTextContent();
      }
    } catch (Exception ex) {
      System.out.println("Pas de document");
    }
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of match method, of class GenItem.
   * @throws java.lang.Exception
   */
  @Test
  public void testMatch() throws Exception {
    System.out.println("match");
    HashMap<Expression, Expression> vars = new HashMap<>();
    HashMap<String, String> typesMap = new HashMap<>();
    ArrayList<Expression> listvars = new ArrayList<>();
    String[] ms = matches.split("\",\\s+\"");
    String[] ls = ms[1].split("\\s"); // liste des variables
    for (String l : ls) {
      Expression v = new Expression(l, syntax);
      listvars.add(v);
    }
    for (int i = 2; i < ms.length - 1; i += 4) {
      typesMap.put(ms[i + 2], ms[i + 3]);      
    }
    GenItem genItem = new GenItem(typesMap, listvars, subtypes);
    for (int i = 2; i < ms.length - 1; i += 4) {
      Expression e = new Expression(ms[i], syntax);
      Expression s = new Expression(ms[i + 1], syntax);
      vars.clear();
      boolean result = genItem.match(e, s, vars);
      System.out.println("" + vars);
      assertEquals(true, result);
    }
    System.out.println("");
  }

  /**
   * Test of matchBoth method, of class GenItem.
   * @throws java.lang.Exception
   */
  @Test
  public void testMatchBoth() throws Exception {
    System.out.println("matchBoth");
    HashMap<String, String> typesMap = new HashMap<>();
    ArrayList<Expression> listvars = new ArrayList<>();
    HashMap<Expression, Expression> evars = new HashMap<>(), svars = new HashMap<>();
    String[] ms = matchBoth.split("\",\\s+\"");
    String[] ls = ms[1].split("\\s");
    for (String l : ls) {
      Expression v = new Expression(l, syntax);
      listvars.add(v);
    }
    GenItem genItem = new GenItem(typesMap, listvars, subtypes);
    for (int i = 2; i < ms.length - 1; i += 4) {
      typesMap.clear();
      typesMap.put(ms[i + 2], ms[i + 3]);
      Expression e = new Expression(ms[i], syntax);
      Expression s = new Expression(ms[i + 1], syntax);
      evars.clear();
      svars.clear();
      boolean fit = genItem.matchBoth(e, s, evars, svars);
      System.out.println("" + evars);
      System.out.println("" + svars);
      System.out.println("e : " + e.toString(syntaxWrite)
              + "\ts : " + s.toString(syntaxWrite) + "\n");
      assertEquals(true, fit);
    }
  }

  /**
   * Test of matchRecursively method, of class GenItem.
   */
  @Ignore
  @Test
  public void testMatchRecursively() {
    System.out.println("matchRecursively");
    Expression e = null;
    Expression s = null;
    HashMap<Expression, Expression> vars = null;
    GenItem instance = null;
    boolean expResult = false;
    boolean result = instance.matchRecursively(e, s, vars);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of matchSubExpr method, of class GenItem.
   * @throws java.lang.Exception
   */
  @Test
  public void testMatchSubExpr() throws Exception {
    System.out.println("matchSubExpr");
    if (matchsubExpr == null) {
      fail("pas de données !");
      return;
    }
    HashMap<String, String> typesMap = new HashMap<>();
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
    for (int i = 2; i < ms.length - 1; i += 5) {      
      typesMap.put(ms[i + 2], ms[i + 3]);
    }
    GenItem genItem = new GenItem(typesMap, listvars, subtypes);
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
      Expression result = genItem.matchSubExpr(e, replaceMap);
      System.out.println(e.toString(syntaxWrite) + "  ->  " + result.toString(syntaxWrite));
      assertEquals(expected, result);
    }
    System.out.println("");
  }

  /**
   * Test of toString method, of class GenItem.
   */
  @Test
  @Ignore
  public void testToString() {
    System.out.println("toString");
    GenItem instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getTypesMap method, of class GenItem.
   */
  @Test
  @Ignore
  public void testGetTypesMap() {
    System.out.println("getTypesMap");
    GenItem instance = null;
    HashMap<String, String> expResult = null;
    HashMap<String, String> result = instance.getTypesMap();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getListvars method, of class GenItem.
   */
  @Test
  @Ignore
  public void testGetListvars() {
    System.out.println("getListvars");
    GenItem instance = null;
    ArrayList<Expression> expResult = null;
    ArrayList<Expression> result = instance.getListvars();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  
  
}
