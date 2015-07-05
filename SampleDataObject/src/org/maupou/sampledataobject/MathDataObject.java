/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.sampledataobject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.maupou.expressions.ExprNode;
import org.maupou.expressions.Expression;
import org.maupou.expressions.Generator;
import org.maupou.expressions.Syntax;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Messages({
  "LBL_Math_LOADER=Files of Math"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Math_LOADER",
        mimeType = "text/x-math",
        extension = {"math"}
)
@DataObject.Registration(
        mimeType = "text/x-math",
        iconBase = "org/maupou/sampledataobject/address-book-open.png",
        displayName = "#LBL_Math_LOADER",
        position = 300
)
@ActionReferences({
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
          position = 100,
          separatorAfter = 200
  ),
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
          position = 300
  ),
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
          position = 400,
          separatorAfter = 500
  ),
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
          position = 600
  ),
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
          position = 700,
          separatorAfter = 800
  ),
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
          position = 900,
          separatorAfter = 1000
  ),
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
          position = 1100,
          separatorAfter = 1200
  ),
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
          position = 1300
  ),
  @ActionReference(
          path = "Loaders/text/x-math/Actions",
          id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
          position = 1400
  )
})

public class MathDataObject extends MultiDataObject {

  private Document mathdoc;
  private final Saver saver = new Saver();

  public MathDataObject(FileObject pf, MultiFileLoader loader)
          throws DataObjectExistsException, IOException {
    super(pf, loader);
    try {
      getCookieSet().add((Node.Cookie) new MathOpenSupport(getPrimaryEntry()));
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      mathdoc = documentBuilder.parse(pf.getInputStream());
    } catch (ParserConfigurationException | SAXException ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  public Syntax setSyntax() throws Exception {
    Syntax syntax = null;
    String path = mathdoc.getDocumentElement().getAttribute("syntax");
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    if (!path.isEmpty()) {
      File syntaxFile = new File(path);
      Document syxdoc = documentBuilder.parse(syntaxFile);
      syntax = new Syntax(syxdoc);
      syntax.addGenerators(syxdoc);
    }
    return syntax;
  }

  /**
   * remplit la liste des expressions correspondant à ce Generator
   * @param gen
   * @return la liste
   * @throws Exception 
   */
  public ArrayList<ExprNode> readExprNodes(Generator gen) throws Exception {
    ArrayList<ExprNode> exprNodes = new ArrayList<>();
    NodeList egens = mathdoc.getElementsByTagName("generator");
    for (int i = 0; i < egens.getLength(); i++) {
      Element egen = (Element) egens.item(i);
      if (gen.getName().equals(egen.getAttribute("name"))) {
        NodeList exprItems = egen.getElementsByTagName("expr");
        for (int k = 0; k < exprItems.getLength(); k++) {
          Element exprItem = (Element) exprItems.item(k);
          NodeList nodes = exprItem.getElementsByTagName("text");
          if (nodes.getLength() != 1) {
            continue;
          }
          String etext = nodes.item(0).getTextContent();
          ArrayList<int[]> parents = new ArrayList<>();
          ArrayList<Integer> childs = new ArrayList<>();
          Expression e = new Expression(etext);
          nodes = exprItem.getElementsByTagName("children");
          if (nodes.getLength() == 1) {
            String echilds = nodes.item(0).getTextContent();
            String[] s = echilds.trim().split(" ");
            for (String item : s) {
              childs.add(Integer.parseInt(item));
            }
          }
          nodes = exprItem.getElementsByTagName("parents");
          if (nodes.getLength() == 1) {
            String eparents = nodes.item(0).getTextContent();
            String[] s = eparents.trim().split(" ");
            for (String string : s) {
              String[] si = string.split("-");
              int[] p = new int[2];
              if (si.length == 2) {
                p[0] = Integer.parseInt(si[0]);
                p[1] = Integer.parseInt(si[1]);
                parents.add(p);
              }
            }
          }
          ExprNode en = new ExprNode(e, childs, parents);
          String id = exprItem.getAttribute("id");
          en.setRange(Integer.parseInt(id));
          exprNodes.add(en);
        }
      }
      break;
    }
    return exprNodes;
  }
  
  /**
   * 
   * @param exprList la liste des expressions à insérer
   * @param range point d'insertion
   * @param generator
   * @throws Exception 
   */
  public void insert(List<ExprNode> exprList, int range, Generator generator) throws Exception {
    NodeList list = mathdoc.getElementsByTagName("generator");
    if(list.getLength() == 0) { // créer l'élément
      Element gen = mathdoc.createElement("generator");
      gen.setAttribute("name", generator.getName());
      mathdoc.getDocumentElement().appendChild(gen);
    }
    for (int i = 0; i < list.getLength(); i++) {
      Element gen = (Element) list.item(i);
      if (generator.getName().equals(gen.getAttribute("name"))) {
        list = gen.getElementsByTagName("expr");
        org.w3c.dom.Node next = (range < list.getLength())? list.item(range).getNextSibling() : null;
        for (ExprNode expr : exprList) {
          Expression e = expr.getE();
          Element elem = (Element) gen.insertBefore(mathdoc.createElement("expr"), next);
          elem.setAttribute("id", "" + expr.getRange());
          elem.setAttribute("type", e.getType());
          org.w3c.dom.Node ntxt = elem.appendChild(mathdoc.createElement("text"));
          ntxt.setTextContent(e.toText());
          String txt = "";
          txt = expr.getParentList().stream().map((p) -> {
            return Arrays.toString(p).replaceAll("[\\[\\],]", "").replace(" ", "-") + " ";
          }).reduce(txt, String::concat).trim();
          if (!txt.isEmpty()) {
            elem.appendChild(mathdoc.createElement("parents")).setTextContent(txt);
          }
          txt = expr.getChildList().toString().replaceAll("[\\[\\],]", "");
          if (!txt.isEmpty()) {
            elem.appendChild(mathdoc.createElement("children")).setTextContent(txt);
          }
        }
        break;
      }
    }
    setModified(true);
    getCookieSet().add(saver);
  }
  
  /**
   * 
   * @param index rang de l'expression à supprimer
   * @param generator 
   */
  public void delete(int index, Generator generator) {
    NodeList list = mathdoc.getElementsByTagName("generator");
    for (int i = 0; i < list.getLength(); i++) {
      Element gen = (Element) list.item(i);
      if (generator.getName().equals(gen.getAttribute("name"))) {
        list = gen.getElementsByTagName("expr");
        if (list.getLength() > index) {
          gen.removeChild(list.item(index));
        }
      }
    }
    setModified(true);
    getCookieSet().add(saver);
  }

  @Override
  public Lookup getLookup() {
    return getCookieSet().getLookup();
  }

  @Override
  protected int associateLookup() {
    return 1;
  }

  private class Saver implements SaveCookie {

    @Override
    public void save() {
      try {
        Document doc;
        synchronized (MathDataObject.this) {
          doc = mathdoc;
          setModified(false);
          getCookieSet().remove(saver);
        }
        FileObject fo = getPrimaryFile();
        String fileDisplayName = FileUtil.getFileDisplayName(fo);
        File file = new File(fileDisplayName);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
      } catch (TransformerConfigurationException ex) {
        Exceptions.printStackTrace(ex);
      } catch (TransformerException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
  }
}
