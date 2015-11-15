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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
public class ProducerTest {

  private static File syxDir;
  private static File tstDir;
  private final String syxfile;
  private Syntax syntax;
  private String[] matches, matchBoth, matchsubExpr, results;
  private boolean fail = false;

  /**
   * nom des fichiers à tester {syntaxe, exemples}
   *
   * @return la liste des noms de fichiers
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Object[][] files = new Object[][]{
      {"number_syntax_1.syx", "0"},
      {"types_1.syx", "1"}
    };
    return Arrays.asList(files);
  }

  public ProducerTest(String s, String t) {
    syxfile = s;
  }

  @BeforeClass
  public static void setUpClass() {
    syxDir = new File("C:/Users/Patrice/Documents/NetBeansProjects/Mathorium/Examples");
    tstDir = new File("C:/Users/Patrice/Documents/NetBeansProjects/Mathorium/datatest");
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    try {
      File syntaxFile = new File(syxDir, syxfile);
      if (!syntaxFile.isFile()) {
        System.out.println("pas de fichier syntaxe : " + syntaxFile);
        return;
      }
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(syntaxFile);
      syntax = new Syntax(document);
      File tstFile = new File(tstDir, "syxwTests.xml");
      document = documentBuilder.parse(tstFile);
      Element syx = document.getElementById(syxfile);
      if (syx == null) {
        fail = true;
      } else {
        matches = getTexts(syx, "match");
        matchBoth = getTexts(syx, "matchBoth");
      }
    } catch (Exception ex) {
      fail = true;
    }
  }

  @After
  public void tearDown() {
  }

  private String[] getTexts(Element syx, String id) {
    NodeList list = syx.getElementsByTagName(id);
    Element elem = (Element) list.item(0);
    String text = elem.getTextContent();
    return text.split("\",\\s+\"");
  }

  /**
   * Test of match method, of class Producer.
   */
  @Test
  public void testMatch() {
    System.out.println("match");
    if (matches == null) {
      return;
    }
    HashMap<Expr, Expr> result = new HashMap<>(), expResult = new HashMap<>();
    ArrayList<SExpr> listvars = new ArrayList<>();
    int k = 1, range = 0;
    while (k < matches.length - 1) {
      range++;
      result.clear();
      expResult.clear();
      listvars.clear();
      String[] ls = matches[k].split(","); // liste des variables
      int n = ls.length;
      for (String l : ls) {
        String[] v = l.split(":");
        listvars.add(new SExpr(v[0], v[1], true));
      }
      Producer prod = new Producer(listvars, syntax.getSubtypes());
      Expr e = syntax.parseExpr(matches[k + 1]);
      Expr s = Expr.scanExpr(matches[k + 2]);
      boolean match = prod.match(e, s, result);
      for (int i = 0; i < n; i++) { // 
        ls = matches[i + k + 3].split(",");
        String[] ks = ls[0].split(":");
        SExpr key = new SExpr(ks[0], ks[1], false);
        Expr value = syntax.parseExpr(ls[1]);
        expResult.put(key, value);
      }
      System.out.println(range + ": " + e + "  " + s + "\t-> " + match);
      assertEquals(expResult, result);
      k = n + k + 3;
    }
  }

  /**
   * Test of matchBoth method, of class Producer.
   */
  //@Ignore
  @Test
  public void testMatchBoth() {
    System.out.println("matchBoth");
    if (matchBoth == null) {
      return;
    }
    HashMap<Expr, Expr> evars = new HashMap<>(), svars = new HashMap<>();
    HashMap<Expr, Expr> expEvars = new HashMap<>(), expSvars = new HashMap<>();
    ArrayList<SExpr> listvars = new ArrayList<>();
    int k = 1, range = 0;
    while (k < matchBoth.length - 1) {
      range++;
      evars.clear();
      svars.clear();
      expEvars.clear();
      expSvars.clear();
      listvars.clear();
      String[] ls = matchBoth[k].split(","); // liste des variables
      int n = ls.length;
      for (String l : ls) {
        String[] v = l.split(":");
        listvars.add(new SExpr(v[0], v[1], true));
      }
      Producer prod = new Producer(listvars, syntax.getSubtypes());
      Expr e = Expr.scanExpr(matchBoth[k + 1]);
      Expr s = Expr.scanExpr(matchBoth[k + 2]);
      boolean match = prod.matchBoth(e, s, evars, svars);
      System.out.println(range + ": " + e + "  " + s + "\t-> " + match);
      System.out.println("\t" + evars + " " + svars);
      fillmap(matchBoth[k + 3], expEvars);
      fillmap(matchBoth[k + 4], expSvars);
      //System.out.println("\t" + expEvars + " " + expSvars);
      assertEquals(expEvars, evars);
      assertEquals(expSvars, svars);
      k += 5;
    }
  }

  private void fillmap(String txt, HashMap<Expr, Expr> map) {
    if (!txt.isEmpty()) {
      String[] ls = txt.split(";");
      for (int i = 0; i < ls.length; i += 2) {
        String[] ks = ls[i].split(":");
        SExpr key = new SExpr(ks[0], ks[1], false);
        Expr value = Expr.scanExpr(ls[i + 1]);
        map.put(key, value);
      }
    }
  }

  /**
   * Test of matchSubExpr method, of class Producer.
   */
  @Ignore
  @Test
  public void testMatchSubExpr() {
    System.out.println("matchSubExpr");
    Expr e = null;
    Expr m = null;
    Expr r = null;
    Producer instance = null;
    Expr expResult = null;
    Expr result = instance.matchSubExpr(e, m, r);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of markUsedVars method, of class Producer.
   */
  @Ignore
  @Test
  public void testMarkUsedVars() {
    System.out.println("markUsedVars");
    Expr e = null;
    Producer instance = null;
    instance.markUsedVars(e);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of varsInExpression method, of class Producer.
   */
  @Test
  @Ignore
  public void testVarsInExpression() {
    System.out.println("varsInExpression");
    Expr e = null;
    ArrayList<Expr> vars = null;
    Producer instance = null;
    instance.varsInExpression(e, vars);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of extendMap method, of class Producer.
   */
  @Ignore
  @Test
  public void testExtendMap() {
    System.out.println("extendMap");
    Expr e = null;
    HashMap<SExpr, Expr> vars = null;
    Producer instance = null;
    instance.extendMap(e, vars);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of setSchema method, of class Producer.
   */
  @Ignore
  @Test
  public void testSetSchema() {
    System.out.println("setSchema");
    Schema2 schema = null;
    Syntax syntax = null;
    Producer instance = null;
    instance.setSchema(schema, syntax);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class Producer.
   */
  @Ignore
  @Test
  public void testToString() {
    System.out.println("toString");
    Producer instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getName method, of class Producer.
   */
  @Ignore
  @Test
  public void testGetName() {
    System.out.println("getName");
    Producer instance = null;
    String expResult = "";
    String result = instance.getName();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of log method, of class Producer.
   */
  @Ignore
  @Test
  public void testLog() {
    System.out.println("log");
    Producer instance = null;
    String expResult = "";
    String result = instance.log();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
