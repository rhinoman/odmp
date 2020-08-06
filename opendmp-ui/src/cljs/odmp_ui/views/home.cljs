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

(ns odmp-ui.views.home
  (:require
   [re-frame.core :as re-frame]
   [breaking-point.core :as bp]
   [odmp-ui.subs :as subs]
   [odmp-ui.components.common :as tcom]
   ["@material-ui/core/Button" :default Button]
   ["@material-ui/core/Typography" :default Typography]))

(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    (tcom/full-content-ui {:title "Dashboard"}
     [:div
      [:> Typography {:variant "h3"} "Open Data Management Platform"]]
     [:div
      [:> Typography {:variant :subtitle1} "TODO: Snazzy charts and graphs go here"]
      [:> Typography {:variant :subtitle1} "TODO: Need a real logo"]]
     [:div
      [:> Typography {:variant :body1}
       "You're looking at an extremely early build of OpenDMP.  It doesn't do much yet."]])))
