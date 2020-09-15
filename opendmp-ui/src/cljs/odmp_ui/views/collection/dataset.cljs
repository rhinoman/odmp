;; Copyright 2020 James Adam and the Open Data Management Platform contributors.

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;; http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns odmp-ui.views.collection.dataset
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.subs :as subs]
            [odmp-ui.events :as events]
            [odmp-ui.util.network :as net]
            [odmp-ui.util.styles :as style]
            [odmp-ui.util.ui :refer [upper-case]]
            [odmp-ui.components.common :as tcom]))


(defn dataset*
  "Display a single dataset record"
  [])


(defn dataset
  [id]
  (r/create-class
   {:reagent-render dataset*
    :component-did-mount
    (fn [])
    :component-will-unmount
    (fn [])}))
