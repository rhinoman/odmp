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

(ns odmp-ui.views.collection.index
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [odmp-ui.util.styles :as style]
   [odmp-ui.util.network :as net]
   [odmp-ui.subs :as subs]
   [odmp-ui.events :as events]
   [odmp-ui.components.common :as tcom]
   [odmp-ui.views.collection.collection-modals :as modals]
   ["@material-ui/core/Link" :default Link]
   ["@material-ui/core/Grid" :default Grid]
   ["@material-ui/core/Toolbar" :default Toolbar]
   ["@material-ui/core/Button" :default Button]
   ["@material-ui/core/Paper" :default Paper]
   ["@material-ui/core/Typography" :default Typography]
   ["@material-ui/core/Tooltip" :default Tooltip]
   ["@material-ui/core/Table" :default Table]
   ["@material-ui/core/TableHead" :default TableHead]
   ["@material-ui/core/TableBody" :default TableBody]
   ["@material-ui/core/TableCell" :default TableCell]
   ["@material-ui/core/TableContainer" :default TableContainer]
   ["@material-ui/core/TableHead" :default TableHead]
   ["@material-ui/core/TablePagination" :default TablePagination]
   ["@material-ui/core/TableRow" :default TableRow]
         
   ["@material-ui/icons/Add" :default AddIcon]

))

(defn collection-index-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:right {:float :right}}))

(defn collection-row [collection classes]
  ^{:key (:id collection)}
  [:> TableRow {:hover true :tabIndex -1}
   [:> TableCell {:class (:collection-item-cell classes)}
    [:> Tooltip {:title (or (:description collection) "No Description") :placement "bottom-start"}
     [:> Link {:class (:link classes)
               :href (str "#/collections/" (:id collection))}
      (:name collection)]]]])

(defn table-header []
  [:> TableHead
   [:> TableRow
    [:> TableCell "Name"]]])

(defn toolbar [classes]
  [:> Toolbar {:disableGutters true}
   [:> Grid {:container true :spacing 2}
    [:> Grid {:item true :xs 9}]
    [:> Grid {:item true :xs 3}
     [:> Button {:color :primary
                 :variant :contained
                 :disableElevation true
                 :onClick #(rf/dispatch [::events/toggle-create-collection-dialog])
                 :class (:right classes)}
      [:> AddIcon] "Create"]]]])

(defn collection-index* []
  (let [collections (rf/subscribe [::subs/collections])
        create-dialog-state (rf/subscribe [::modals/create-collection-dialog-open])]
    (style/let [classes collection-index-styles]
      [:<>
       [tcom/breadcrumbs (list {:href "#/collections" :text "Collection Index"})]
       [tcom/full-content-ui {:title "Collections"}
        (if @create-dialog-state (modals/create-collection-dialog))
        [:div
         [toolbar classes]
         [:> Paper
          (if (nil? @collections) [tcom/loading-backdrop])
          [:> TableContainer
           [:> Table
            [table-header]
            [:> TableBody
             (if (> (count @collections) 0)
               (map #(collection-row % classes) @collections)
               [:> TableRow
                [:> TableCell "No Collections to Display"]])]]]]]]])))

(defn collection-index
  []
  (r/create-class
   {:reagent-render collection-index*
    :component-did-mount
    (fn [_]
      (net/auth-dispatch [::events/fetch-collection-list]))}))
