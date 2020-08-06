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
   [odmp-ui.subs :as subs]
   [odmp-ui.components.common :as tcom]))

(defn collection-index-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {}))

(defn collection-index []
  (let [collections (rf/subscribe [::subs/collections])]
    (style/let [classes collection-index-styles]
      [:<>
       [tcom/breadcrumbs (list {:href "#/collections" :text "Collection Index"})]
       [tcom/full-content-ui {:title "Collections"}]])))
