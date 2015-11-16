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
  private String[] matches, matchBoth, matchSubExpr;

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
      matches = getTexts(syx, "match");
      matchBoth = getTexts(syx, "matchBoth");
      matchSubExpr = getTexts(syx, "matchSubExpr");
    } catch (Exception ex) {
      fail("Nothing to test : " + ex.getMessage());
    }
  }

  @After
  public void tearDown() {
  }

  private String[] getTexts(Element syx, String id) {
    String[] ret = null;
    NodeList list = syx.getElementsByTagName(id);
    if (list.getLength() == 1) {
      Element elem = (Element) list.item(0);
      String text = elem.getTextContent();
      ret = text.split("\",\\s+\"");
    }
    return ret;
  }

  /**
   * Test of match method, of class Producer.
   */
  @Test
  public void testMatch() {
    System.out.println("match");
    if (matches != null) {
      HashMap<Expr, Expr> result = new HashMap<>();
      int k = 1, range = 0;
      while (k < matches.length - 1) {
        range++;
        result.clear();
        ArrayList<SExpr> listvars = getVars(matches[k++]);
        Producer prod = new Producer(listvars, syntax.getSubtypes());
        Expr e = syntax.parseExpr(matches[k++]);
        Expr s = Expr.scanExpr(matches[k++]);
        boolean match = prod.match(e, s, result);
        HashMap<Expr, Expr> expResult = getMap(matches[k++], true);
        System.out.println(range + ": " + e + "  " + s + "\t-> " + match);
        assertEquals(expResult, result);
      }
      System.out.println("");
    }
  }

  /**
   * Test of matchBoth method, of class Producer.
   */
  @Test
  public void testMatchBoth() {
    System.out.println("matchBoth");
    if (matchBoth != null) {
      HashMap<Expr, Expr> evars = new HashMap<>(), svars = new HashMap<>();
      int k = 1, range = 0;
      while (k < matchBoth.length - 1) {
        range++;
        evars.clear();
        svars.clear();
        ArrayList<SExpr> listvars = getVars(matchBoth[k++]);
        Producer prod = new Producer(listvars, syntax.getSubtypes());
        Expr e = Expr.scanExpr(matchBoth[k++]);
        Expr s = Expr.scanExpr(matchBoth[k++]);
        boolean match = prod.matchBoth(e, s, evars, svars);
        System.out.println(range + ": " + e + "  " + s + "\t-> " + match);
        System.out.println("\t" + evars + " " + svars);
        HashMap<Expr, Expr> expEvars = getMap(matchBoth[k++], false);
        HashMap<Expr, Expr> expSvars = getMap(matchBoth[k++], false);
        //System.out.println("\t" + expEvars + " " + expSvars);
        assertEquals(expEvars, evars);
        assertEquals(expSvars, svars);
      }
      System.out.println("");
    }
  }

  

  private void listOfVars(String txt, ArrayList<SExpr> listvars) {
    String[] ls = txt.split(","); // liste des variables
    listvars.clear();
    for (String l : ls) {
      String[] v = l.split(":");
      listvars.add(new SExpr(v[0], v[1], true));
    }
  }
  

  /**
   * Test of matchSubExpr method, of class Producer.
   */
  //@Ignore
  @Test
  public void testMatchSubExpr() {
    System.out.println("matchSubExpr");
    if (matchSubExpr != null) {
      HashMap<Expr, Expr> vars = new HashMap<>();
      int k = 1, range = 0;
      ArrayList<SExpr> listvars = getVars(matchSubExpr[k++]);
      while (k < matchSubExpr.length - 1) {
        range++;
        vars.clear();
        Expr m0 = syntax.parseExpr(matchSubExpr[k++]);
        Expr m1 = syntax.parseExpr(matchSubExpr[k++]);
        Expr e = syntax.parseExpr(matchSubExpr[k++]);
        Expr expResult = syntax.parseExpr(matchSubExpr[k++]);
        Producer prod = new Producer(listvars, syntax.getSubtypes());
        Expr result = prod.matchSubExpr(e, m0, m1);
        System.out.println(range + ": " + e + " " + result);
        assertEquals(range + ": ", expResult, result);
      }
      System.out.println("");
    }
  }
  
  private ArrayList<SExpr> getVars(String txt) {
    ArrayList<SExpr> listvars = new ArrayList<>();
    String[] ls = txt.split(","); // liste des variables
    listvars.clear();
    for (String l : ls) {
      String[] v = l.split(":");
      listvars.add(new SExpr(v[0], v[1], true));
    }
    return listvars;
  }
  
  private HashMap<Expr, Expr> getMap(String txt, boolean withSyntax) {
    HashMap<Expr, Expr> map = new HashMap<>();
    if (!txt.isEmpty()) {
      String[] ls = txt.split(";");
      for (int i = 0; i < ls.length; i += 2) {
        String[] ks = ls[i].split(":");
        SExpr key = new SExpr(ks[0], ks[1], false);
        Expr value;
        if (withSyntax) {
          value = syntax.parseExpr(ls[i + 1]);
        } else {
          value = Expr.scanExpr(ls[i + 1]);
        }
        map.put(key, value);
      }
    }
    return map;
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
