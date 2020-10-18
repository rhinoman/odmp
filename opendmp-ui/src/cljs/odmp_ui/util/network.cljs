(ns odmp-ui.util.network
  (:require [re-frame.core :as rf]
            [odmp-ui.subs :as subs]))

(defn auth-dispatch [event-vec]
  (let [auth @(rf/subscribe [::subs/authentication])]
    (if (and (some? (:keycloak auth)) (true? (:authenticated auth)))
      (rf/dispatch event-vec))))

;; Network events, eh.

(defn get-token [db]
  (let [keycloak (get-in db [:auth-state :keycloak])]
    (.-token keycloak)))

(defn basic-headers [db]
  {:Authorization (str "Bearer " (get-token db))})
