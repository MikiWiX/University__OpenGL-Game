package renderEngine.loaders.collada;

import java.util.*;

public class XmlNode {
    private String name;
    private Map<String, String> attributes;
    private String data;
    private Map<String, List<XmlNode>> childNodes;

    protected XmlNode(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getData() {
        return this.data;
    }

    public String getAttribute(String attr) {
        return this.attributes != null ? (String)this.attributes.get(attr) : null;
    }

    public XmlNode getChild(String childName) {
        if (this.childNodes != null) {
            List<XmlNode> nodes = (List)this.childNodes.get(childName);
            if (nodes != null && !nodes.isEmpty()) {
                return (XmlNode)nodes.get(0);
            }
        }

        return null;
    }

    public XmlNode getChildWithAttribute(String childName, String attr, String value) {
        List<XmlNode> children = this.getChildren(childName);
        if (children != null && !children.isEmpty()) {
            Iterator var6 = children.iterator();

            while(var6.hasNext()) {
                XmlNode child = (XmlNode)var6.next();
                String val = child.getAttribute(attr);
                if (value.equals(val)) {
                    return child;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public List<XmlNode> getChildren(String name) {
        if (this.childNodes != null) {
            List<XmlNode> children = (List)this.childNodes.get(name);
            if (children != null) {
                return children;
            }
        }

        return new ArrayList();
    }

    protected void addAttribute(String attr, String value) {
        if (this.attributes == null) {
            this.attributes = new HashMap();
        }

        this.attributes.put(attr, value);
    }

    protected void addChild(XmlNode child) {
        if (this.childNodes == null) {
            this.childNodes = new HashMap();
        }

        List<XmlNode> list = (List)this.childNodes.get(child.name);
        if (list == null) {
            list = new ArrayList();
            this.childNodes.put(child.name, list);
        }

        ((List)list).add(child);
    }

    protected void setData(String data) {
        this.data = data;
    }
}
