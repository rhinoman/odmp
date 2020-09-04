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
            [odmp-ui.events :as events]
            [odmp-ui.util.network :as net]
            [odmp-ui.util.styles :as style]
            [odmp-ui.util.ui :refer [upper-case]]
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
            ["@material-ui/core/TableSortLabel" :default TableSortLabel]
            ["@material-ui/core/TableRow" :default TableRow]))


(defn collection-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {}))

(defn fetch-dataset-page [page-state]
  (let [collection (rf/subscribe [::subs/current-collection])
        page-state* (reduce-kv (fn [m k v]
                                 (cond
                                   (nil? @v) m
                                   (or (= @v "asc") (= @v "desc")) (assoc m k (upper-case @v))
                                   :else (assoc m k @v))) {} page-state)]
    (rf/dispatch [::events/fetch-collection-datasets
                  (:id @collection) page-state*])
    (rf/dispatch [::events/fetch-collection-dataset-count (:id @collection)])))

(defn flip-sort-dir [cur-dir]
  (if (= cur-dir "asc") "desc" "asc"))

(defn sortable-header [field page-state]
  [:> TableCell 
   [:> TableSortLabel {:active (= @(:sortBy page-state) (:name field))
                       :direction (if (= @(:sortBy page-state) (:name field)) @(:sortDir page-state) "asc")
                       :onClick (fn [] (reset! (:sortBy page-state) (:name field))
                                       (reset! (:sortDir page-state) (flip-sort-dir @(:sortDir page-state)))
                                       (fetch-dataset-page page-state))}
    (:label field)]])

(defn table-header [page-state]
  [:> TableHead
   [:> TableRow
    (map (fn [field]
           ^{:key (str (:name field) "_HEADER")}
           [sortable-header field page-state])
         (list {:name "name" :label "Data Set"}
               {:name "createdOn" :label "Created"}
               {:name "destinationType" :label "Destination"}
               {:name "location" :label "Location"}))]])

(defn dataset-row [dataset classes]
  ^{:key (:id dataset)}
  [:> TableRow {:hover true :tabIndex -1}
   [:> TableCell {:width "30%"} (:name dataset)]
   [:> TableCell {:width "20%"} (:createdOn dataset)]
   [:> TableCell {:width "10%"} (:destinationType dataset)]
   [:> TableCell [:> Link {:href (:location dataset) :download true} (:location dataset)]]])

(defn pagination [num-items page-state classes]
  [:> TablePagination {:component "div"
                       :rowsPerPageOptions [5 10 25]
                       :rowsPerPage @(:maxPerPage page-state)
                       :page @(:page page-state)
                       :count num-items
                       :onChangeRowsPerPage (fn [e]
                                              (reset! (:maxPerPage page-state) (-> e .-target .-value))
                                              (fetch-dataset-page page-state))
                       :onChangePage (fn [e np]
                                       (reset! (:page page-state) np)
                                       (fetch-dataset-page page-state))}])

(defn collection*
  "Display a collection"
  []
  (let [collection (rf/subscribe [::subs/current-collection])
        datasets   (rf/subscribe [::subs/current-collection-datasets])
        num-datasets (rf/subscribe [::subs/current-collection-dataset-count])
        page-state {:page (r/atom 0) :maxPerPage (r/atom 10) :sortBy (r/atom nil) :sortDir (r/atom nil)}]
    (fn [] (style/let [classes collection-styles]
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
             [table-header page-state]
             [:> TableBody
              (if (> (count @datasets) 0)
                (map #(dataset-row % classes) @datasets)
                [:> TableRow
                 [:> TableCell "No Datasets to Display"]])]]]
           (if (some? @num-datasets)
             [pagination @num-datasets page-state classes])]]]]))))


(defn collection
  [id]
  (r/create-class
   {:reagent-render collection*
    :component-did-mount
    (fn []
      (net/auth-dispatch [::events/fetch-collection id])
      (net/auth-dispatch [::events/fetch-collection-datasets id {:page 0 :maxPerPage 10}])
      (net/auth-dispatch [::events/fetch-collection-dataset-count id]))
    :component-will-unmount
    (fn []
      (rf/dispatch-sync [::events/clear-collection-data]))}))
