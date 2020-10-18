var config = {
    "_id": "rs0",
    "members": [{
        "_id": 0,
        "host": "mongodb:27017"
    }]
};

var err = rs.initiate(config, { force: true });
printjson(err);
rs.status();
