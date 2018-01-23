package com.denimgroup.threadfix.framework.impl.jsp;

import com.denimgroup.threadfix.framework.util.CodeParseUtil;
import com.denimgroup.threadfix.framework.util.EventBasedTokenizer;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class JSPElementReferenceParser {

    public JSPElementReferenceParser() {

    }

    public JSPElementReferenceParser(List<String> watchedElementTypes) {
        watchedElements = new ArrayList<String>(watchedElementTypes);
    }

    private List<String> watchedElements = list("a", "input", "form", "textarea", "select");

    public List<JSPElementReference> parse(@Nonnull File file) {
        String fileContents = null;

        Stack<JSPElementReference> elementStack = new Stack<JSPElementReference>();
        List<JSPElementReference> rootElements = list();

        try {
            fileContents = FileUtils.readFileToString(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        fileContents = stripJspElements(fileContents);
        Document doc;

        try {
            doc = Jsoup.parse(fileContents);
        } catch (Exception anyException) {
            anyException.printStackTrace();
            return null;
        }

        if (doc == null) {
            return null;
        }

        for (Node child : doc.childNodes()) {
            processNode(child, elementStack, rootElements);
        }

        assignSourceFileToElements(rootElements, file.getAbsolutePath());
        return rootElements;
    }

    private void processNode(Node htmlNode, Stack<JSPElementReference> elementStack, List<JSPElementReference> rootElements) {
        boolean pushedStack = false;
        if (watchedElements.contains(htmlNode.nodeName())) {

            JSPElementReference newElement = new JSPElementReference();
            newElement.setElementType(htmlNode.nodeName());

            for (Attribute attr : htmlNode.attributes()) {
                newElement.addAttribute(attr.getKey(), attr.getValue());
            }

            if (elementStack.isEmpty()) {
                rootElements.add(newElement);
            } else {
                elementStack.peek().addChild(newElement);
            }

            elementStack.push(newElement);

            pushedStack = true;
        }

        for (Node childNode : htmlNode.childNodes()) {
            processNode(childNode, elementStack, rootElements);
        }

        if (pushedStack) {
            elementStack.pop();
        }
    }

    private void assignSourceFileToElements(List<JSPElementReference> elements, String sourceFilePath) {
        Stack<JSPElementReference> pendingElements = new Stack<JSPElementReference>();
        pendingElements.addAll(elements);

        while (!pendingElements.isEmpty()) {
            JSPElementReference current = pendingElements.pop();
            current.setSourceFile(sourceFilePath);

            pendingElements.addAll(current.getChildren());
        }
    }

    private String stripJspElements(String jspFileContents) {
        return Pattern.compile("<%[^%>]*%>", Pattern.DOTALL).matcher(jspFileContents).replaceAll("");
    }
}
