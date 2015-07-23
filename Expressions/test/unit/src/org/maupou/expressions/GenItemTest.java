/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Patrice
 */
public class GenItemTest {

  private final String syxfile, tstfile;

  /**
   * nom des fichiers à tester {syntaxe, exemples}
   *
   * @return la liste des noms de fichiers
   */
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Object[][] files = new Object[][]{
      {"number_syntax.syx", "numbers.math"}//, {"types.syx", "typesTests.xml"}
    };
    return Arrays.asList(files);
  }
  private Syntax syntax;
  private SyntaxWrite syntaxWrite;
  private ArrayList<ExprNode> exprNodes;

  public GenItemTest(String s, String t) {
    syxfile = s;
    tstfile = t;
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws Exception {
    File directory = new File("C:/Users/Patrice/Documents/NetBeansProjects/Mathorium/Examples");
    File syntaxFile = new File(directory, syxfile);
    File expressions = new File(directory, tstfile);
    if (!syntaxFile.isFile() && expressions.exists()) {
      System.out.println("pas de fichier syntaxe : " + syntaxFile);
      return;
    }
    if (!expressions.exists()) {
      System.out.println("pas de fichier test : " + expressions);
    }
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(syntaxFile);
    syntax = new Syntax(document);
    syntaxWrite = syntax.getSyntaxWrite();
    document = documentBuilder.parse(expressions);
    exprNodes = new ArrayList<>();
    NodeList exprTexts = document.getElementsByTagName("text");
    for (int i = 0; i < exprTexts.getLength(); i++) {
      Expression e = new Expression(exprTexts.item(i).getTextContent());
      exprNodes.add(new ExprNode(e, new ArrayList<>(), new ArrayList<>()));
    }
  }

  @After
  public void tearDown() {
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

  

  /**
   * Test of getMatchExprs method, of class GenItem.
   */
  @Test
  @Ignore
  public void testGetMatchExprs() {
    System.out.println("getMatchExprs");
    GenItem instance = null;
    ArrayList<MatchExpr> expResult = null;
    ArrayList<Schema> result = instance.getSchemas();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

 

}
