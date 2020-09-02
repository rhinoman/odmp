(ns odmp-ui.util.ui)

(defn ignore-return [e]
  (if (= (.-keyCode e) 13) (.preventDefault e)))

(defn upper-case [s]
  (if (nil? s) nil (clojure.string/upper-case s)))
