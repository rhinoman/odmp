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
            [odmp-ui.components.common :as tcom]
            [odmp-ui.views.collection.collection-modals :as c-modals]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/Link" :default Link]
            ["@material-ui/core/Paper" :default Paper]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Tooltip" :default Tooltip]
            ["@material-ui/core/IconButton" :default IconButton]
            ["@material-ui/icons/DeleteTwoTone" :default DeleteIcon]
            ["@material-ui/core/Table" :default Table]
            ["@material-ui/core/TableHead" :default TableHead]
            ["@material-ui/core/TableBody" :default TableBody]
            ["@material-ui/core/TableCell" :default TableCell]
            ["@material-ui/core/TableContainer" :default TableContainer]
            ["@material-ui/core/TableHead" :default TableHead]
            ["@material-ui/core/TableRow" :default TableRow]))


(defn dataset-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:delete-dataset-wrapper {:float :right
                              :margin-top 0}
     :property-wrapper {:padding 10
                        :marbin-bottom 20}
     :history-table-wrapper {:padding 10}
     :property-table {}}))

(defn header [field]
  [:> TableCell (:label field)])

(defn history-table-header []
  [:> TableHead
   [:> TableRow
    (map (fn [field]
           ^{:key (str (:name field) "_HEADER")}
           [header field])
         (list {:name "timestamp" :label "Time"}
               {:name "eventType" :label "Event Type"}
               {:name "processorName" :label "Processor"}))]])

(defn event-row [event]
  [:> TableRow {:hover true :tabIndex -1}
   [:> TableCell {:width "40%"} (:timestamp event)]
   [:> TableCell {:width "20%"} (:eventType event)]
   [:> TableCell
    [:> Link {:href (str "#/processors/" (:processorId event))}
     (:processorName event)]]])

(defn history-table [history]
  [:> TableContainer
   [:> Table
    [history-table-header]
    [:> TableBody 
     (map-indexed
      (fn [idx evt] ^{:key (str "HISTORY_" idx)} [event-row evt]) history)]]])

(defn request-download [event dataset]
  (.preventDefault event)
  (rf/dispatch [::events/request-dataset-download (:id dataset)]))

(defn properties-table [dataset classes]
  [:> TableContainer
   [:> Table {:class (:property-table classes) :size :small}
    [:> TableBody
     [:> TableRow
      [:> TableCell "Data Tag"] [:> TableCell (:dataTag @dataset)]]
     [:> TableRow
      [:> TableCell "Created"] [:> TableCell (:createdOn @dataset)]]
     [:> TableRow
      [:> TableCell "Data Destination"] [:> TableCell (:destinationType @dataset)]]
     [:> TableRow
      [:> TableCell "Location"] [:> TableCell [:> Link {:href "#"
                                                        :onClick #(request-download % @dataset)}
                                               (:location @dataset)]]]]]])

(defn dataset*
  "Display a single dataset record"
  []
  (let [dataset (rf/subscribe [::subs/current-dataset])
        collection (rf/subscribe [::subs/current-collection])
        delete-dialog? (rf/subscribe [::c-modals/delete-dataset-dialog-open])]
    (fn [] (style/let [classes dataset-styles]
       [:<>
        (if @delete-dialog? (c-modals/confirm-delete-dataset @dataset (:id @collection)))
        [:> Box [tcom/breadcrumbs (list {:href "#/collections" :text "Collection Index"}
                                        {:href (str "#/collections/" (:id @collection)) :text (:name @collection)}
                                        {:href (str "#/datasets/" (:id @dataset)) :text (:name @dataset)})]

         [:div {:class (:delete-dataset-wrapper classes)}
          [:> Tooltip {:title "Delete this dataset" :placement :left-end}
           [:> IconButton {:color :secondary
                           :onClick #(rf/dispatch [::c-modals/toggle-delete-dataset-dialog])
                           :size :small}
            [:> DeleteIcon]]]]]
        [tcom/full-content-ui
         {:title (:name @dataset)}
         [:iframe {:style {:display :none} :id "downloaderIframe"}]
         (if (nil? @dataset)
           [tcom/loading-backdrop]
           [:> Paper 
            [:> Box {:class (:history-table-wrapper classes)}
             [:> Typography {:variant :h5} "Properties"]
             [:> Box {:class {:property-wrapper classes}}
              [properties-table dataset classes]]
             [:> Typography {:variant :h5 :style {:margin-top 20}} "History"]
             [history-table (get-in @dataset [:history 0])]]])]]))))

(defn dataset
  [id]
  (r/create-class
   {:reagent-render dataset*
    :component-did-mount
    (fn []
      (net/auth-dispatch [::events/fetch-dataset id]))
    :component-will-unmount
    (fn []
      (rf/dispatch-sync [::events/clear-collection-data]))}))
