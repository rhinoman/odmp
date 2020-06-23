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

(ns odmp-ui.views.dataflow.index
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.components.common :as tcom]
            [odmp-ui.util.styles :as style]
            [odmp-ui.subs :as subs]
            ["@material-ui/core/Table" :default Table]
            ["@material-ui/core/TableBody" :default TableBody]
            ["@material-ui/core/TableCell" :default TableCell]
            ["@material-ui/core/TableContainer" :default TableContainer]
            ["@material-ui/core/TableHead" :default TableHead]
            ["@material-ui/core/TablePagination" :default TablePagination]
            ["@material-ui/core/TableRow" :default TableRow]
            ["@material-ui/core/TableSortLabel" :default TableSortLabel]
            ["@material-ui/core/Toolbar" :default Toolbar]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Paper" :default Paper]))


(defn dataflow-styles [^js/Mui.Theme theme] {})

(defn dataflow-index []
  (let [dataflows (rf/subscribe [::subs/dataflows])]
    (style/let [classes dataflow-styles]
      (tcom/full-content-ui {:title "Data Flows"}
        [:> Paper
         [:> TableContainer
          [:> Table
           [:> TableBody
            [:> TableRow {:hover true :tabIndex -1}
             [:> TableCell "Boo"]]]]]]))))
