;; Taken from https://gist.github.com/mhuebert/4e3af2fbc33b40e74539487e50110a14
(ns odmp-ui.util.styles
  #?(:clj (:refer-clojure :exclude [let]))
  #?(:cljs (:require ["@material-ui/core/styles" :refer [withStyles]]
                     [react]
                     [reagent.core :as r]))
  #?(:cljs (:require-macros odmp-ui.util.styles)))

#?(:cljs
   (defn with-styles
     "Given a map of {classKey, styles} (or a function which takes a theme & returns such a map),
      returns the same map of {classKey, className}"
     [styles-or-fn body-fn]
     (let [hoc (withStyles
                (if (fn? styles-or-fn)
                  (comp clj->js styles-or-fn)
                  (clj->js styles-or-fn)))
           body-component (r/reactify-component
                           (fn [{:keys [classes]}]
                             [body-fn (-> classes
                                          (js->clj :keywordize-keys true))]))]
       (react/createElement
        (hoc body-component)))))

#?(:clj
   (defmacro let [bindings & body]
     (loop [[var-name style-expr :as bindings] bindings
            out `(r/as-element
                  (do ~@body))]
       (if (empty? bindings)
         out
         (recur (drop 2 bindings)
                `[~'odmp-ui.util.styles/with-styles
                  ~style-expr
                  (fn [~var-name] ~out)])))))
