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

(ns odmp-ui.views.dataflow.flow
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.subs :as subs]
            [odmp-ui.util.styles :as style]
            [odmp-ui.util.data :as dutil]
            [odmp-ui.components.common :as tcom]
            [odmp-ui.subs :as subs]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/Toolbar" :default Toolbar]
            ["@material-ui/core/Paper" :default Paper]
            ["@material-ui/icons/AddTwoTone" :default AddIcon]))


(defn flow-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:right {:float :right}
     :proc-wrapper {:min-height 400
                    :padding 10}}))

(defn toolbar [classes]
  [:> Toolbar {:disableGutters true}
   [:> Grid {:container true :spacing 2}
    [:> Grid {:item true :xs 9}]
    [:> Grid {:item true :xs 3}
     [:> Button {:color :primary :variant :contained :disableElevation true :class (:right classes)}
      [:> AddIcon] "Create"]]]])

(defn flow []
  (let [dataflow (rf/subscribe [::subs/current-dataflow])
        processors (rf/subscribe [::subs/current-dataflow-processors])
        num-phases (dutil/num-phases @processors)]
    (println num-phases)
    (style/let [classes flow-styles]
      (tcom/full-content-ui {:title (:name @dataflow)}
       [:> Typography {:variant :subtitle1} (:description @dataflow)]
       (toolbar classes)
       [:> Paper {:class (:proc-wrapper classes)} "HI"]))))
