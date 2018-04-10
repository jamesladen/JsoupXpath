package org.seimicrawler.xpath;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.antlr.XpathLexer;
import org.seimicrawler.xpath.antlr.XpathParser;
import org.seimicrawler.xpath.core.XValue;
import org.seimicrawler.xpath.core.XpathProcessor;
import org.seimicrawler.xpath.exception.DoFailOnErrorHandler;
import org.seimicrawler.xpath.exception.XpathParserException;
import org.seimicrawler.xpath.exception.XpathSyntaxErrorException;

import java.util.LinkedList;
import java.util.List;

/*
   Copyright 2014 Wang Haomiao<seimimaster@gmail.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 */
public class JXDocument {
    private Elements elements;

    public JXDocument(Elements els){
        elements = els;
    }

    public static JXDocument create(Document doc){
        Elements els = doc.children();
        return new JXDocument(els);
    }

    public static JXDocument create(Elements els){
        return new JXDocument(els);
    }

    public static JXDocument create(String html){
        Elements els  = Jsoup.parse(html).children();
        return new JXDocument(els);
    }

    public static JXDocument createByUrl(String url){
        Elements els;
        try {
            els = Jsoup.connect(url).get().children();
        } catch (Exception e) {
            throw new XpathParserException("url资源获取失败",e);
        }
        return new JXDocument(els);
    }

    public List<Object> sel(String xpath) throws XpathSyntaxErrorException {
        List<Object> res = new LinkedList<>();
        for (JXNode node:selN(xpath)){
            if (node.isText()){
                res.add(node.getTextVal());
            }else {
                res.add(node.getElement());
            }
        }
        return res;
    }

    public List<JXNode> selN(String xpath) throws XpathSyntaxErrorException{
        List<JXNode> finalRes = new LinkedList<>();
        try {
            CharStream input = CharStreams.fromString(xpath);
            XpathLexer lexer = new XpathLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            XpathParser parser = new XpathParser(tokens);
            parser.setErrorHandler(new DoFailOnErrorHandler());
            ParseTree tree = parser.main();
            XpathProcessor processor = new XpathProcessor(elements);
            XValue calRes = processor.visit(tree);
            if (calRes.isElements()){
                for (Element el:calRes.asElements()){
                    finalRes.add(JXNode.e(el));
                }
            }else if (calRes.isList()){
                for (String str:calRes.asList()){
                    finalRes.add(JXNode.t(str));
                }
            }else if (calRes.isString()){
                finalRes.add(JXNode.t(calRes.asString()));
            }else if (calRes.isNumber()){
                finalRes.add(JXNode.t(calRes.asDouble().toString()));
            }
        } catch (Exception e){
            String msg = "Please check the syntax of your xpath expr, ";
            throw new XpathSyntaxErrorException(msg+ExceptionUtils.getRootCauseMessage(e),e);
        }
        return finalRes;
    }

    public Object selOne(String xpath) throws XpathSyntaxErrorException {
    	JXNode jxNode = selNOne(xpath);
    	if(jxNode != null) {
    		if (jxNode.isText()){
                return jxNode.getTextVal();
            }else {
                return jxNode.getElement();
            }
    	}
    	return null;
    }

    public JXNode selNOne(String xpath) throws XpathSyntaxErrorException {
    	List<JXNode> jxNodeList = selN(xpath);
    	if(jxNodeList != null && jxNodeList.size() > 0) {
    		return jxNodeList.get(0);
    	}
    	return null;
    }
}
