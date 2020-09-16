(ns odmp-ui.util.ui
  (:require [cljs-time.format :as fmt]))

(defn ignore-return [e]
  (if (= (.-keyCode e) 13) (.preventDefault e)))

(defn upper-case [s]
  (cond
      (nil? s) nil
      (string? s) (clojure.string/upper-case s)
      :else s))

(defn format-time
  "Turns a raw datetime into a nicely formatted string"
  [t]
  (let [dt (fmt/parse t)
        nice-fmt (fmt/formatter "yyyy MMM dd HH:mm:ss.S")]
       (fmt/unparse nice-fmt dt)))
