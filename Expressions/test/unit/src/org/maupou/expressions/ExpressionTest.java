/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.junit.*;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.junit.runners.Parameterized.*;

/**
 *
 * @author Patrice Maupou
 */
@RunWith(Parameterized.class)

public class ExpressionTest {

  private Syntax syntax;
  private SyntaxWrite syntaxWrite;
  private String[] entries;
  private String[] complete;
  private String[] results;
  private String[] printing;
  private String replacements, matches, matchBoth, matchsubExpr;
  private String depths;

  private final String syxfile, tstfile;
  private static File sysDir, tstDir;

  /**
   * nom des fichiers à tester {syntaxe, exemples}
   * @return la liste des noms de fichiers
   */
  @Parameters
  public static Collection<Object[]> data() {
    Object[][] files = new Object[][]{
      {"number_syntax.syx", "expressionsTests.xml"}, {"types.syx", "typesTests.xml"}
    };
    return Arrays.asList(files);
  }

  public ExpressionTest(String s, String t) {
    syxfile = s;
    tstfile = t;
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    sysDir = new File("C:/Users/Patrice/Documents/NetBeansProjects/Mathorium/Examples");
    tstDir = new File("C:/Users/Patrice/Documents/NetBeansProjects/Mathorium/datatest");  
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
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
      document = documentBuilder.parse(expressions);
      Element texts = document.getElementById("exprs");
      String exprsText = texts.getTextContent();
      entries = exprsText.split("\",\\s+\"");
      texts = document.getElementById("results");
      String resultsText = texts.getTextContent();
      results = resultsText.split("\",\\s+\"");
      Element completeText = document.getElementById("complete");
      complete = completeText.getTextContent().split("\",\\s+\"");
      texts = document.getElementById("write_numbers");
      printing = texts.getTextContent().split("\",\\s+\"");
      // remplacements
      texts = document.getElementById("replace");
      if (texts != null) {
        replacements = texts.getTextContent();
      }
      texts = document.getElementById("matches");
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
      texts = document.getElementById("depth");
      depths = texts.getTextContent();
    } catch (Exception ex) {
      System.out.println("Pas de document");
    }
  }

  @After
  public void tearDown() {
  }

  
 
  @Test
  //@Ignore
  public void testReplace() {
    if (replacements == null) {
      return;
    }
    System.out.println("replace");
    String[] replaces = replacements.split("\",\\s+\"");
    for (int i = 1; i < replaces.length - 1; i += 3) {
      String eTxt = replaces[i];
      Expression e = syntax.parse(eTxt);
      String[] maptxt = replaces[i + 1].split(",");
      String rTxt = replaces[i + 2];
      Expression result = syntax.parse(rTxt);
      HashMap<Expression, Expression> map = new HashMap<>();
      for (String maptxt1 : maptxt) {
        String[] couple = maptxt1.split("=");
        assertEquals("pas d'égalité", couple.length, 2);
        map.put(syntax.parse(couple[0]), syntax.parse(couple[1]));
      }
      e = e.replace(map);
      assertEquals(eTxt + " mal transformé", result, e);
    }
  }

 
  /**
   * Test of toString method, of class Expression.
   *
   */
  @Test
  //@Ignore
  public void testToString() {
    if (results == null) {
      return;
    }
    System.out.println("toString");
    int n = entries.length;
    int m = results.length;
    m = (n < m)? n : m;
    for (int i = 1; i < m-1; i++) {
      String entry = entries[i];
      Expression exp = syntax.parse(entry);
      String expString = exp.toString();
      String expected = results[i];
      System.out.println(i + ":  " + entry + "  ->  " + expString + "   type: " + exp.getType());
      assertEquals(expected, expString);
    }
    System.out.println("\n");
  }

  @Test
  //@Ignore
  public void testToText() {
    if (complete == null) {
      return;
    }
    System.out.println("toText");
    int n = entries.length;
    int m = complete.length;
    m = (n < m)? n : m;
    for (int i = 1; i < m-1; i++) {
      String entry = entries[i];
      Expression exp = syntax.parse(entry);
      String expString = exp.toText();
      String expected = complete[i];
      System.out.println(i + ": " + expString);
      assertEquals(expected, expString);
    }
    System.out.println("\n");
  }

  /**
   * Test of scanExpr method, of class Expression.
   *
   * @throws java.lang.Exception
   */
  @Test
  //@Ignore
  public void testScanExpr() throws Exception {
    if (complete == null) {
      return;
    }
    System.out.println("scanExpr");
    for (int i = 1; i < complete.length - 1; i++) {
      String text = complete[i];
      Expression expected = syntax.parse(entries[i]);
      ArrayList<Expression> list = new ArrayList<>();
      String[] ret = Expression.scanExpr(text, list);
      assertEquals(expected, list.get(0));
      System.out.println(i + " :" + entries[i] + "    size: " + list.size()
              + " marqueur: " + ret[0] + "text: " + ret[1]);
    }
    System.out.println("\n");
  }

  @Test
  //@Ignore
  public void testToString_syntaxWrite() {
    if (printing == null) {
      return;
    }
    System.out.println("toString(syntaxWrite)");
    int n = entries.length;
    int m = printing.length;
    if(n < m) m = n;
    for (int i = 1; i < m - 1; i++) {
      String entry = entries[i];
      Expression exp = syntax.parse(entry);
      String expString;
      try {
        expString = syntaxWrite.toString(exp);
      } catch (Exception ex) {
        expString = "";
      }
      String expected = printing[i];
      System.out.println(i + ":  " + entry + "  ->  " + expString + "   type: " + exp.getType());
      assertEquals(expected, expString);
    }
  }

}
