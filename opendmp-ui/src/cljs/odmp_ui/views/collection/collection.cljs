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

(ns odmp-ui.views.collection.collection
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.subs :as subs]
            [odmp-ui.util.styles :as style]
            [odmp-ui.components.common :as tcom]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/Link" :default Link]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/Paper" :default Paper]
            ["@material-ui/core/Toolbar" :default Toolbar]
            ["@material-ui/icons/DeleteTwoTone" :default DeleteIcon]
            ["@material-ui/core/Table" :default Table]
            ["@material-ui/core/TableHead" :default TableHead]
            ["@material-ui/core/TableBody" :default TableBody]
            ["@material-ui/core/TableCell" :default TableCell]
            ["@material-ui/core/TableContainer" :default TableContainer]
            ["@material-ui/core/TableHead" :default TableHead]
            ["@material-ui/core/TablePagination" :default TablePagination]
            ["@material-ui/core/TableRow" :default TableRow]))


(defn collection-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {}))

(defn table-header []
  [:> TableHead
   [:> TableRow
    [:> TableCell "Data Set"]
    [:> TableCell "Created"]
    [:> TableCell "Destination"]
    [:> TableCell "Location"]]])

(defn dataset-row [dataset classes]
  ^{:key (:id dataset)}
  [:> TableRow {:hover true :tabIndex -1}
   [:> TableCell (:name dataset)]
   [:> TableCell (:createdOn dataset)]
   [:> TableCell (:destinationType dataset)]
   [:> TableCell [:> Link {:href (:location dataset) :download true} (:location dataset)]]])

(defn collection
  "Display a collection"
  []
  (let [collection (rf/subscribe [::subs/current-collection])
        datasets   (rf/subscribe [::subs/current-collection-datasets])]
    (style/let [classes collection-styles]
      [:<>
       [:> Box [tcom/breadcrumbs (list {:href "#/collections" :text "Collection Index"}
                                       {:href (str "#/collections/" (:id @collection)) :text (:name @collection)})]
       [tcom/full-content-ui
        {:title (:name @collection)}
        (if (nil? @collection) [tcom/loading-backdrop])
        [:> Paper
         (if (nil? @datasets) [tcom/loading-backdrop])
         [:> TableContainer
          [:> Table
           [table-header]
           [:> TableBody
            (if (> (count @datasets) 0)
              (map #(dataset-row % classes) @datasets)
              [:> TableRow
               [:> TableCell "No Datasets to Display"]])]]]]]]])))

