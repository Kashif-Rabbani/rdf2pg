/*
 * Copyright 2020 Renzo Angles (http://renzoangles.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import pgraph.PGEdge;
import pgraph.PGNode;
import pgraph.PGProperty;
import writers.PGWriter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 */
public class Neo4jWriter implements PGWriter {

    Writer writer;
    String filename = "output.ypg";
    HashMap<Integer, Integer> oidmap = new HashMap();
    int oid = 1;

    public Neo4jWriter(String _filename) {
        this.filename = _filename;
    }

    @Override
    public void begin() {
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        } catch (Exception ex) {
            System.out.println("Error1: " + ex.getMessage());
        }
    }

    private void writeLine(String line) {
        try {
            writer.write(line);
        } catch (Exception ex) {
            System.out.println("Error2: " + ex.getMessage());
        }

    }

    @Override
    public void writeNode(PGNode node) {
        Integer node_id = oidmap.get(node.getId());
        if (node_id == null) {
            node_id = oid;
            oidmap.put(node.getId(), oid);
            oid++;
        }

        String labels = "";
        Iterator<String> it1 = node.getLabels();
        while (it1.hasNext()) {
            String label = it1.next();
            labels = labels + ":" + label;
        }

        int cnt = 0;
        String props = "";
        Iterator<PGProperty> it2 = node.getProperties();
        while (it2.hasNext()) {
            PGProperty prop = it2.next();
            cnt++;
            if (cnt < node.propertiesCounter()) {
                props = props + prop.getLabel() + ":'" + prop.getValue() + "'" + ",";
            } else {
                props = props + prop.getLabel() + ":'" + prop.getValue() + "'";
            }
        }

        // CREATE (n:Person:Swedish { name: 'Andy', title: 'Developer' })
        /*
        CREATE (n12:nss1_Professor:nss1_Faculty:nss1_Person {id: 'n12', iri: 'http://x.y/alice', nss1_name: 'Alice Smith'});
        CREATE (n21:nss1_University {id: 'n21', iri: 'http://x.y/MIT', nss1_name: 'Massachusetts Institute of Technology'});

         */
        String line;
        if (props.compareTo("") == 0) {
            line = "CREATE (n" + node.getId() + labels + " {" + "id:'n" + node.getId() + "' } );\n";
        } else {
            line = "CREATE (n" + node.getId() + labels + " {" + "id:'n" + node.getId() + "' ," + props + "} );\n";
        }
        this.writeLine(line);
    }

    @Override
    public void writeEdge(PGEdge edge) {
        Integer edge_id = oidmap.get(edge.getId());
        if (edge_id == null) {
            edge_id = oid;
            oidmap.put(edge.getId(), oid);
            oid++;
        }

        String labels = "";
        Iterator<String> it1 = edge.getLabels();
        while (it1.hasNext()) {
            String label = it1.next();
            labels = labels + ":" + label;
        }

        int cnt = 0;
        String props = "";
        Iterator<PGProperty> it2 = edge.getProperties();
        while (it2.hasNext()) {
            PGProperty prop = it2.next();
            cnt++;
            if (cnt < edge.propertiesCounter()) {
                props = props + prop.getLabel() + ":\"" + prop.getValue() + "\"" + ",";
            } else {
                props = props + prop.getLabel() + ":\"" + prop.getValue() + "\"";
            }
        }

        Integer snode_oid = oidmap.get(edge.getSourceNode());
        if (snode_oid == null) {
            snode_oid = oid;
            oidmap.put(edge.getSourceNode(), oid);
            oid++;
        }
        Integer tnode_oid = oidmap.get(edge.getTargetNode());
        if (tnode_oid == null) {
            tnode_oid = oid;
            oidmap.put(edge.getTargetNode(), oid);
            oid++;
        }

        snode_oid = edge.getSourceNode();
        tnode_oid = edge.getTargetNode();


        // CREATE (a)-[r:RELTYPE]->(b)
        /*
        MATCH (n12 {id: 'n12'}), (n21 {id: 'n21'})
        CREATE (n12)-[e23:nss1_docDegreeFrom]->(n21);
         */
        String line;
        if (edge.propertiesCounter() == 0) {
            line = "MATCH (n" + snode_oid + " {id: 'n" + snode_oid + "'}), (n" + tnode_oid + " {id: 'n" + tnode_oid + "'})" + " CREATE (n" + snode_oid + ")-[e" + edge_id + labels + "]->(n" + tnode_oid + ");" + "\n";
            //line = "CREATE (n" + snode_oid + ")-[e" + edge_id + labels + "]->(n" + tnode_oid + ")" + "\n";
        } else {
            line = "MATCH (n" + snode_oid + " {id: 'n" + snode_oid + "'}), (n" + tnode_oid + " {id: 'n" + tnode_oid + "'})" + " CREATE (n" + snode_oid + ")-[e" + edge_id + labels + " {n" + props + "}]->(n" + tnode_oid + ");" + "\n";
            //line = "CREATE (n" + snode_oid + ")-[e" + edge_id + labels + " {n" + props + "}]->(n" + tnode_oid + ")" + "\n";
        }
        this.writeLine(line);
    }

    @Override
    public void end() {
        try {
            writer.close();
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

}
