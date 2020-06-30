(defproject opendmp-ui "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.748"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.8.109"]
                 [reagent "0.10.0"]
                 [re-frame "0.12.0"]
                 [day8.re-frame/http-fx "v0.2.0"]
                 [day8.re-frame/tracing "0.5.5"]
                 [day8.re-frame/forward-events-fx "0.0.6"]
                 [secretary "1.2.3"]
                 [cljs-http "0.1.46"]
                 [ns-tracker "0.4.0"]
                 [re-pressed "0.3.1"]
                 [breaking-point "0.1.2"]
                 [cider/cider-nrepl "0.24.0"]]

  :plugins [[lein-shadow "0.1.7"]
            [lein-shell "0.5.0"]]
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0" }

  :min-lein-version "2.5.3"

  :jvm-opts ["-Xmx1G"]

  :source-paths ["src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "resources/public/css"]

  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :shadow-cljs {:nrepl {:port 8777}
                
                :builds {:app {:target :browser
                               :js-options {:resolve {"keycloak-js" {:target :global
                                                                     :global "Keycloak"}}}
                               :output-dir "resources/public/js/compiled"
                               :asset-path "/js/compiled"
                               :modules {:app {:init-fn odmp-ui.core/init
                                               :preloads [devtools.preload
                                                          ;day8.re-frame-10x.preload
                                                          ]}}
                               :dev {:compiler-options {:closure-defines {re-frame.trace.trace-enabled? true
                                                                          day8.re-frame.tracing.trace-enabled? true
                                                                          }}}
                               :release {:build-options
                                         {:ns-aliases
                                          {day8.re-frame.tracing day8.re-frame.tracing-stubs}}}

                               :devtools {:http-root "resources/public"
                                          :http-port 8280
                                          }}}}

  :aliases {"dev"          ["with-profile" "dev" "do"
                            ["shadow" "watch" "app"]]
            "prod"         ["with-profile" "prod" "do"
                            ["shadow" "release" "app"]]
            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]
            "karma"        ["with-profile" "prod" "do"
                            ["shadow" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.0"]
                   [day8.re-frame/re-frame-10x "0.6.4"]]
    :source-paths ["dev"]}

   :prod {}
   
}

  :prep-tasks [])
