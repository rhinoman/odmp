#!/bin/bash

set -e

mongo <<EOF
use admin

db.createUser({
  user: '$DATAFLOW_DB_USERNAME',
  pwd: '$DATAFLOW_DB_PW',
  roles: [{
    role: 'readWrite',
    db: 'odmp_dataflow'
  }]
})

EOF
