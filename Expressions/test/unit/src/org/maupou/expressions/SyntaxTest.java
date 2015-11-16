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
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Patrice
 */
@RunWith(Parameterized.class)
public class SyntaxTest {
  
  private Syntax syntax;
  private String[] entries;
  private String[] results;
  private final String syxfile;
  private static File syxDir, tstDir;
  private  boolean fail = false;
  
  /**
   * nom des fichiers à tester {syntaxe, exemples}
   * @return la liste des noms de fichiers
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Object[][] files = new Object[][]{
      {"number_syntax_1.syx", "syntaxTests.xml"}, 
      {"types_1.syx", "typesTests.xml"}
    };
    return Arrays.asList(files);
  }
  
  public SyntaxTest(String s, String t) {
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
        entries = getTexts(syx, "exprs");
        results = getTexts(syx, "results");
      }
    } catch (Exception ex) {
      fail = true; 
    }
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
   * Test of parseExpr method, of class Syntax.
   */
  @Test
  public void testParseExpr() {
    if (!fail) {
      System.out.println("parseExpr : " + syxfile);
      int n = entries.length;
      int m = results.length;
      m = (n < m) ? n : m;
      for (int i = 1; i < m - 1; i++) {
        String text = entries[i];
        String message = i + " : " + text;
        Expr e = syntax.parseExpr(text);
        String result = e.toString();
        String expResult = results[i];
        System.out.println(message + "  " + result);
        assertEquals(message, expResult, result);
      }
    }
    System.out.println("\n");
  }

  
  /**
   * Test of getSyntaxWrites method, of class Syntax.
   */
  @Test
  @Ignore
  public void testGetSyntaxWrites() {
    System.out.println("getSyntaxWrites");
    Syntax instance = null;
    HashMap<String, SyntaxWrite> expResult = null;
    HashMap<String, SyntaxWrite> result = instance.getSyntaxWrites();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  

  /**
   * Test of getRules method, of class Syntax.
   */
  @Test
  @Ignore
  public void testGetRules() {
    System.out.println("getRules");
    Syntax instance = null;
    ArrayList<SyntaxRule> expResult = null;
    ArrayList<SyntaxRule> result = instance.getRules();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

   

  /**
   * Test of toString method, of class Syntax.
   */
  @Test
  @Ignore
  public void testToString() {
    System.out.println("toString");
    Syntax instance = null;
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
  
}
