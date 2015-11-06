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
import java.util.List;
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
public class SyntaxWriteTest {

  /**
   * nom des fichiers à tester {syntaxe, exemples}
   *
   * @return la liste des noms de fichiers
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Object[][] files = new Object[][]{
      {"number_syntax_1.syx", "0"} , {"types_1.syx", "1"}
    };
    return Arrays.asList(files);
  }

  private static File syxDir;
  private static File tstDir;

  private Syntax syntax;
  private SyntaxWrite syntaxWrite;
  private final String syntaxName;
  private final String range;
  private boolean fail;
  private String[] entries;
  private String[] results;

  public SyntaxWriteTest(String s, String t) {
    syntaxName = s;
    range = t;
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
      File syntaxFile = new File(syxDir, syntaxName);
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(syntaxFile);
      syntax = new Syntax(document);
      syntaxWrite = syntax.getSyntaxWrite();
      File tstFile = new File(tstDir, "syxwTests.xml");
      document = documentBuilder.parse(tstFile);
      Element syx = document.getElementById(syntaxName);
      if (syx == null) {
        fail = true;
        return;
      } else {
        entries = getTexts(syx, "toText");
        results = getTexts(syx, "toString");
      }
    } catch (Exception ex) {
      System.out.println("erreur : syntaxe indisponible");
      fail = true;
      return;
    }
    fail = false;
  }
  
  private String[] getTexts(Element syx, String id) {
    NodeList list = syx.getElementsByTagName(id);
    Element elem = (Element) list.item(0);
    String text = elem.getTextContent();
    return text.split("\",\\s+\"");
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of toString method, of class SyntaxWrite.
   */
  @Ignore
  @Test
  public void testToString_Expression() {
    if(fail) return;
    System.out.println("toString (Expression");
    Expression e = null;
    SyntaxWrite instance = null;
    String expResult = "";
    String result = instance.toString(e);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class SyntaxWrite.
   */
  @Test
  public void testToString_Expr() {
    if(fail) return;
    System.out.println("toString(Expr) : " + syntaxName);
    List<Expr> exprs = new ArrayList<>();
    for (int i = 1; i < entries.length-1; i++) {
      String entry = entries[i];
      Expr e = Expr.scanExpr(entry);
      if (e != null) {
        exprs.add(e);
      } else {
        fail("pas d'expressions pour " + i + " : " + entry);
      }
    }
    int n = exprs.size()+1;
    if(results.length < n) {
      n = results.length;
    }
    for (int i = 1; i < n; i++) {
      Expr e = exprs.get(i-1);
      String expectedResult = results[i];
      String result = syntaxWrite.toString(e);
      System.out.println(i + ": " + expectedResult + "   " + result);
      assertEquals(expectedResult, result);
    }
    System.out.println("\n");
  }

  /**
   * Test of toString method, of class SyntaxWrite.
   */
  @Ignore
  @Test
  public void testToString_0args() {
    System.out.println("toString");
    SyntaxWrite instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getNameToNode method, of class SyntaxWrite.
   */
  @Ignore
  @Test
  public void testGetNameToNode() {
    System.out.println("getNameToNode");
    SyntaxWrite instance = null;
    HashMap<String, NodeWrite> expResult = null;
    HashMap<String, NodeWrite> result = instance.getNameToNode();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getNodeWrites method, of class SyntaxWrite.
   */
  @Ignore
  @Test
  public void testGetNodeWrites() {
    System.out.println("getNodeWrites");
    SyntaxWrite instance = null;
    ArrayList<NodeWrite> expResult = null;
    ArrayList<NodeWrite> result = instance.getNodeWrites();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getName method, of class SyntaxWrite.
   */
  @Test
  @Ignore
  public void testGetName() {
    System.out.println("getName");
    SyntaxWrite instance = null;
    String expResult = "";
    String result = instance.getName();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
