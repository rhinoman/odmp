conn = new Mongo();
db = conn.getDB("odmp_dataflow");

db.createCollection("dataflows");

db.createCollection("processors");
