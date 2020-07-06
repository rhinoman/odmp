;; Copyright 2020 The Open Data Management Platform contributors.

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;; http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns odmp-ui.util.data)

(defn extract-by-id
  "Extracts the first record from a collection with a matching id field"
  [id coll]
  (first (filter #(= (:id %) id) coll)))

(defn update-by-id
  "Replaces an item with a matching id in a collection"
  [rec coll]
  (map (fn [b] (if (= (:id rec) (:id b)) rec b)) coll))

(defn num-phases
  [processors]
  (if (empty? processors) 0
      (max (map (fn [p] (:phase p)) (reverse processors)))))

