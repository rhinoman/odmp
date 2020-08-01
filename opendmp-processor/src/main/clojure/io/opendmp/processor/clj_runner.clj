;
; Copyright (c) 2020. The Open Data Management Platform contributors.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

; Executes a block of clojure
(ns io.opendmp.processor.clj-runner)

(defn execute
  "Executes a block of clojure code and returns the result as a byte array"
  #^bytes [^String code]
  (byte-array (eval (read-string code))))
