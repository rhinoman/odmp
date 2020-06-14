db.getCollection("dataflows").insertMany([
  {
    _id: ObjectId("5ee4307417b27ddedc18c5f6"),
    name: "TPS Reports",
    description: "Ingest, process, and store TPS reports for analysis",
    group: "",
    creator: "",
    status: "idle",
    health: {
      state: "OK",
      last_error: null,
      last_error_time: null
    },
    created_on: new Date(),
    updated_on: new Date()
  }
])

db.getCollection("processors").insertMany([
  {
    _id: ObjectId(),
    flow_id: "5ee4307417b27ddedc18c5f6",
    type: "INGESTOR",
    status: "idle",
    health: {
      state: "OK",
      last_error: null,
      last_error_time: null
    },
    created_on: new Date(),
    modified_on: new Date(),
    x_pos: 0,
    y_pos: 0,
    trigger: {},
    action: {}
  }
])
