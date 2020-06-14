(ns odmp-dataflow.handler.schema
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]))

(s/def ::_id string?)

(s/def ::name string?)
(s/def ::description string?)
(s/def ::creator (s/nilable string?))
(s/def ::group (s/nilable string?))
(s/def ::status string?)
(s/def ::created_on inst?)
(s/def ::updated_on (s/nilable inst?))

(s/def ::state string?)

(s/def ::last_error (s/nilable string?))
(s/def ::last_error_time (s/nilable string?))

(s/def ::new-flow (s/keys :req-un [::name ::description]
                          :opt-un [::group]))

(s/def ::health (s/keys :req-un [::state]
                        :opt-un [::last_error ::last_error_time]))

(s/def ::dataflow (s/keys :req-un [::_id ::name ::description ::status ::health ::created_on]
                          :opt-un [::group ::creator ::updated_on]))

