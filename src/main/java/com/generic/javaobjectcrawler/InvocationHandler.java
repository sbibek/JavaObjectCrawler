package com.generic.javaobjectcrawler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvocationHandler {

	private final String node_parent = "parent";
	private final String node_type = "type";
	private final String node_name = "name";

	private class Node {
		private String nodeType = "";
		private String nodeData = "";
		private Node parent = null;
		private List<Method> methods = new ArrayList<>();
		private List<Node> children = new ArrayList<>();

		public void addChild(Node node) {
			children.add(node);
		}

		public String getNodeType() {
			return nodeType;
		}

		public void setNodeType(String nodeType) {
			this.nodeType = nodeType;
		}

		public List<Node> getChildren() {
			return children;
		}

		public void setChildren(List<Node> children) {
			this.children = children;
		}

		public String getNodeData() {
			return nodeData;
		}

		public void setNodeData(String nodeData) {
			this.nodeData = nodeData;
		}

		public Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
		}

		public List<Method> getMethods() {
			return methods;
		}

		public void setMethods(List<Method> methods) {
			this.methods = methods;
		}
	}

	private class Tree {
		private Node node;

		public Tree() {
			node = new Node();
		}

		private Node getNodeForType(String type, String data, List<Node> nodes, Node parent, Boolean flag) {
			for (Node node : nodes) {
				if (node.getNodeType().equalsIgnoreCase(type) && node.getNodeData().equalsIgnoreCase(data)) {
					return node;
				}
			}
			// now if we are here then there is no such node, so if flag is true
			// then create
			// the node and return
			if (flag) {
				Node newNode = new Node();
				newNode.setNodeType(type);
				newNode.setParent(parent);
				newNode.setNodeData(data);
				nodes.add(newNode);
				return newNode;
			}
			return null;
		}

		private List<Node> readNodeForType(String type, String data, List<Node> nodes) {
			List<Node> result = new ArrayList<>();
			for (Node node : nodes) {
				if (node.getNodeType().equalsIgnoreCase(type)
						&& (node.getNodeData().equalsIgnoreCase(data) || node.getNodeData().equalsIgnoreCase("*"))) {
					result.add(node);
				}
			}
			return result;
		}

		public Node getNode(String parent, String type, String name) {
			// if such nodes exist, it provides us that node or else
			// new node is created and returned
			Node parentNode = getNodeForType(node_parent, parent, node.getChildren(), node, true);
			// now we have node for the parent, so now we move on to type
			Node typeNode = getNodeForType(node_type, type, parentNode.getChildren(), parentNode, true);
			// Now we have typeNode, we move on to the name node
			Node nameNode = getNodeForType(node_name, name, typeNode.getChildren(), typeNode, true);
			return nameNode;
		}

		public List<Node> readNodes(String parent, String type, String name) {
			List<Node> parentNode = readNodeForType(node_parent, parent, node.getChildren());
			if (parentNode == null)
				return null;

			List<Node> typeNode = new ArrayList<>();
			for (Node n : parentNode) {
				typeNode.addAll(readNodeForType(node_type, type, n.getChildren()));
			}

			List<Node> nameNodes = new ArrayList<>();
			for (Node n : typeNode) {
				nameNodes.addAll(readNodeForType(node_name, name, n.getChildren()));
			}

			return nameNodes;
		}

		private void dump(List<Node> nodes) {
			for (Node n : nodes) {
				System.out.println(n.getNodeType() + "  " + n.getNodeData());
				if (n.getChildren().size() != 0) {
					dump(n.getChildren());
				}
			}
		}

		public void dump() {
			dump(node.getChildren());
		}
	}

	Object invokationObject;
	Tree resolutionTree;

	public InvocationHandler(Object invocationTargetClassObject) {
		this.invokationObject = invocationTargetClassObject;

		// initialize the tree
		resolutionTree = new Tree();
	}

	public void buildInvocationTree(Map<Annotation, Method> meta) {
		// lets add nodes to the tree according to the metadata obtained
		for (Map.Entry<Annotation, Method> entry : meta.entrySet()) {
			Match matchAnnotation = (Match) entry.getKey();
			// now we will append things to the tree according to the
			// information in the annotation
			resolutionTree.getNode(matchAnnotation.parent(), matchAnnotation.type(), matchAnnotation.name())
					.getMethods().add(entry.getValue());
		}
		// resolutionTree.dump();
	}

	public void invoke(String parent, String type, String name,List<String> hierarchy,Object contextSubject,Field field) {
		try {
			//System.out.println("Trying to invoke for ("+parent+","+type+","+name+")");
			List<Node> nodes = resolutionTree.readNodes(parent, type, name);
			if (nodes != null) {
				// means we have something to invoke
				for (Node node : nodes) {
					for (Method m : node.getMethods()) {
						m.invoke(this.invokationObject,hierarchy,contextSubject,field);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
