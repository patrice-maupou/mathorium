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

  /**
   * nom des fichiers à tester {syntaxe, exemples}
   * @return la liste des noms de fichiers
   */
  @Parameters
  public static Collection<Object[]> data() {
    Object[][] files = new Object[][]{
      {"number_syntax.syx", "expressionsTests.xml"}//, {"types.syx", "typesTests.xml"}
    };
    return Arrays.asList(files);
  }

  public ExpressionTest(String s, String t) {
    syxfile = s;
    tstfile = t;
  }

  @BeforeClass
  public static void setUpClass() throws Exception {    
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {    
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
      texts = document.getElementById("matchsubExpr2");
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
  public void testMatchsubExpr() throws Exception {
    if (matchsubExpr == null) {
      return;
    }
    System.out.println("matchsubExpr");
    boolean[] modifs = new boolean[]{false};
    HashMap<String, Set<String>> subtypes = syntax.getSubtypes();
    HashMap<Expression, Expression> vars = new HashMap<>();
    HashMap<Expression, Expression> replaceMap = new HashMap<>();
    HashMap<String, String> typesMap = new HashMap<>();
    ArrayList<Expression> listvars = new ArrayList<>();
    String[] ms = matchsubExpr.split("\",\\s+\"");
    String separator = ms[0];
    String[] ls = ms[1].split("\\s");
    for (String l : ls) {
      Expression v = new Expression(l, syntax);
      listvars.add(v);
    }
    for (int i = 2; i < ms.length - 1; i += 5) {
      Expression e = new Expression(ms[i], syntax);
      replaceMap.clear();
      String[] replace = ms[i + 1].split(separator);
      for (int j = 0; j < replace.length; j += 2) {
        Expression key = new Expression(replace[j], syntax);
        Expression value = new Expression(replace[j + 1], syntax);
        replaceMap.put(key, value);
      }
      typesMap.put(ms[i + 2], ms[i + 3]);
      Expression expected = new Expression(ms[i + 4], syntax);
      vars.clear();
      Expression result = e.matchsubExpr(replaceMap, modifs, typesMap, listvars,
              vars, subtypes);

      System.out.println("" + vars);
      assertEquals(expected, result);
    }
  }

  @Test
  //@Ignore
  public void testMatch() throws Exception {
    if (matches == null) {
      return;
    }
    System.out.println("match");
    HashMap<String, Set<String>> subtypes = syntax.getSubtypes();
    HashMap<Expression, Expression> replace = new HashMap<>();
    HashMap<String, String> freevars = new HashMap<>();
    ArrayList<Expression> listvars = new ArrayList<>();
    String[] ms = matches.split("\",\\s+\"");
    String[] ls = ms[1].split("\\s");
    for (String l : ls) {
      Expression v = new Expression(l, syntax);
      listvars.add(v);
    }
    for (int i = 2; i < ms.length - 1; i += 4) {
      Expression e = new Expression(ms[i], syntax);
      Expression schema = new Expression(ms[i + 1], syntax);
      freevars.put(ms[i + 2], ms[i + 3]);
      replace.clear();
      boolean fit = e.match(schema, freevars, listvars, replace, subtypes);
      System.out.println("" + replace);
      assertEquals(true, fit);
    }
  }

  @Test  
  //@Ignore
  public void testMatchBoth() throws Exception {
    if (matchBoth == null) {
      return;
    }
    System.out.println("matchBoth");
    HashMap<String, Set<String>> subtypes = syntax.getSubtypes();
    HashMap<Expression, Expression> replace = new HashMap<>();
    HashMap<Expression, Expression> sreplace = new HashMap<>();
    ArrayList<Expression> listvars = new ArrayList<>();
    String[] ms = matchBoth.split("\",\\s+\"");
    String[] ls = ms[1].split("\\s");
    for (String l : ls) {
      Expression v = new Expression(l, syntax);
      listvars.add(v);
    }
    for (int i = 2; i < ms.length - 1; i += 4) {
      Expression e = new Expression(ms[i], syntax);
      Expression schema = new Expression(ms[i + 1], syntax);
      HashMap<String, String> freevars = new HashMap<>();
      freevars.put(ms[i + 2], ms[i + 3]);
      replace.clear();
      sreplace.clear();
      boolean fit = e.matchBoth(schema, freevars, listvars, replace, sreplace, subtypes);
      System.out.println("" + replace);
      System.out.println("" + sreplace);
      System.out.println("e : " + e.toString(syntaxWrite)
              + "\tschema : " + schema.toString(syntaxWrite) + "\n");
      assertEquals(true, fit);
    }
  }

  /*
   @Test
   @Ignore
   public void testmarkUsedVars() throws Exception {
        
   }
   //*/
  @Test
  //@Ignore
  public void testReplace() throws Exception {
    if (replacements == null) {
      return;
    }
    System.out.println("replace");
    String[] replaces = replacements.split("\",\\s+\"");
    for (int i = 1; i < replaces.length - 1; i += 3) {
      String eTxt = replaces[i];
      Expression e = new Expression(eTxt, syntax);
      String[] maptxt = replaces[i + 1].split(",");
      String rTxt = replaces[i + 2];
      Expression result = new Expression(rTxt, syntax);
      HashMap<Expression, Expression> map = new HashMap<>();
      for (String maptxt1 : maptxt) {
        String[] couple = maptxt1.split("=");
        assertEquals("pas d'égalité", couple.length, 2);
        map.put(new Expression(couple[0], syntax), new Expression(couple[1], syntax));
      }
      e = e.replace(map);
      assertEquals(eTxt + " mal transformé", result, e);
    }
  }

  @Test
  //@Ignore   
  public void testDepth() throws Exception {
    if (depths == null) {
      return;
    }
    System.out.println("depth");
    String[] depthtxts = depths.split("\",\\s+\"");
    for (int i = 1; i < depthtxts.length - 1; i += 2) {
      Expression e = new Expression(depthtxts[i], syntax);
      int depth = Integer.parseInt(depthtxts[i + 1]);
      assertEquals("profondeur inadéquate sur " + e.toString(syntaxWrite), depth, e.depth());
    }
  }

  /**
   * Test of toString method, of class Expression.
   *
   * @throws Exception
   */
  @Test
  //@Ignore
  public void testToString() throws Exception {
    if (results == null) {
      return;
    }
    System.out.println("toString");
    int n = entries.length;
    int m = results.length;
    m = (n < m)? n : m;
    for (int i = 1; i < m-1; i++) {
      String entry = entries[i];
      Expression exp = new Expression(entry, syntax);
      String expString = exp.toString();
      String expected = results[i];
      System.out.println(i + ":  " + entry + "  ->  " + expString + "   type: " + exp.getType());
      assertEquals(expected, expString);
    }
    System.out.println("\n");
  }

  @Test
  //@Ignore
  public void testToText() throws Exception {
    if (complete == null) {
      return;
    }
    System.out.println("toText");
    int n = entries.length;
    int m = complete.length;
    m = (n < m)? n : m;
    for (int i = 1; i < m-1; i++) {
      String entry = entries[i];
      Expression exp = new Expression(entry, syntax);
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
      Expression expected = new Expression(entries[i], syntax);
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
  public void testToString_syntaxWrite() throws Exception {
    if (printing == null) {
      return;
    }
    System.out.println("toString(syntaxWrite)");
    int n = entries.length;
    int m = printing.length;
    if(n < m) m = n;
    for (int i = 1; i < m - 1; i++) {
      String entry = entries[i];
      Expression exp = new Expression(entry, syntax);
      String expString;
      try {
        expString = exp.toString(syntaxWrite);
      } catch (Exception ex) {
        expString = "";
      }
      String expected = printing[i];
      System.out.println(i + ":  " + entry + "  ->  " + expString + "   type: " + exp.getType());
      assertEquals(expected, expString);
    }
  }

}
